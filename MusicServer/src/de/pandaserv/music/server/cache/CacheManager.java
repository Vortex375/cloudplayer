package de.pandaserv.music.server.cache;

import com.adamtaft.eb.EventBusService;
import de.pandaserv.music.server.database.CacheDatabase;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.events.PrepareCompleteEvent;
import de.pandaserv.music.server.events.PrepareFailedEvent;
import de.pandaserv.music.server.misc.StringUtil;
import de.pandaserv.music.shared.FileStatus;
import de.pandaserv.music.shared.Priority;
import de.pandaserv.music.shared.TrackLength;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ich
 */
public class CacheManager {
    private class CacheMap extends HashMap<Long, CacheEntry> {
        @Override
        public CacheEntry put(Long key, CacheEntry value) {
            if (containsKey(key)) {
                // remove size of previous entry
                currentCacheSize -= get(key).getFileSize();
            }
            currentCacheSize += value.getFileSize();
            return super.put(key, value);
        }

        @Override
        public CacheEntry remove(Object key) {
            if (containsKey(key)) {
                CacheEntry entry = get(key);
                currentCacheSize -= entry.getFileSize();
            }
            return super.remove(key);
        }

        @Override
        public void clear() {
            super.clear();
            currentCacheSize = 0;
        }
    }

    static Logger logger = LoggerFactory.getLogger(CacheManager.class);

    private CacheMap cacheMap;
    private long maxCacheSize;
    private long currentCacheSize;
    private Deque<Long> priorityQueue;
    private File cacheDir;
    private String transcodeCommand;

    // Singleton
    private static CacheManager ourInstance;
    public static CacheManager getInstance() {
        return ourInstance;
    }
    
    public static CacheManager setup(Properties config) {
        if (ourInstance != null) {
            logger.warn("CacheManager.setup() called but there is already an instance!");
        } else {
            ourInstance = new CacheManager(config);
        }

        return ourInstance;
    }
    
    private CacheManager(Properties config) {
        cacheMap = new CacheMap();
        currentCacheSize = 0;
        maxCacheSize = StringUtil.parseSize(config.getProperty("cache_max_size"));
        logger.info("max cache size: {}", maxCacheSize);
        priorityQueue = new LinkedList<>();
        transcodeCommand = config.getProperty("transcode_cmd");

        // prepare cache directory
        String cachePath = config.getProperty("cache_dir");
        cacheDir = new File(cachePath);

        indexCache();
    }

    private void indexCache() {
        logger.info("indexing cache...");
        List<CacheDatabase.Entry> entries = CacheDatabase.getInstance().listEntries();

        int count = 0;
        for (CacheDatabase.Entry entry: entries) {
            long id = entry.getId();
            File f = new File(cacheDir.getAbsolutePath() + "/" + id);

            if (!f.exists()) {
                logger.info("Indexed file not found, removing from index: {}", id);
                // file in cache index was deleted on disk
                CacheDatabase.getInstance().removeEntry(id);
                continue;
            }

            TrackLength length = TrackDatabase.getInstance().getTrackLength(id);

            if (length == null) {
                // file in cache index was deleted from database
                // delete the file from index and on disk
                logger.info("Indexed file is not in track database, removing from index: {}", id);
                CacheDatabase.getInstance().removeEntry(id);
                f.delete();
                continue;
            }

            if (f.length() != length.getFileSize()) {
                // file size was changed!?
                logger.info("Size mismatch, removing file from index: {}", id);
                CacheDatabase.getInstance().removeEntry(id);
                f.delete();
                continue;
            }

            if (entry.getAvailable() > f.length()) {
                // insane cache entry
                logger.info("Invalid cache entry: available bytes is larger than file size: {}", id);
                CacheDatabase.getInstance().removeEntry(id);
                f.delete();
                continue;
            }

            /*
             * all checks passed, create new cache entry
             */
            CacheEntry cacheEntry = new CacheEntry(id);
            cacheEntry.setAvailable(entry.getAvailable());
            cacheEntry.setDuration(length.getDuration());
            cacheEntry.setFileSize(length.getFileSize());

            cacheMap.put(id, cacheEntry);
            count++;
        }
        logger.info("{} files indexed", count);
    }
    
    public synchronized InputStream getInputStream(long id, int offsetSeconds) throws IOException {
        CacheEntry cacheEntry;
        if (!cacheMap.containsKey(id)) {
            /*
             * create new cache entry
             */
            cacheCleanup();

            TrackLength length = TrackDatabase.getInstance().getTrackLength(id);
            if (length == null) {
                logger.info("getInputStream(): unknown track: {}", id);
                return null;
            }

            cacheEntry = new CacheEntry(id);
            cacheEntry.setFileSize(length.getFileSize());
            cacheEntry.setDuration(length.getDuration());
            cacheEntry.setAvailable(0);

            cacheMap.put(id, cacheEntry);

            priorityQueue.addFirst(id);
        } else {
            cacheEntry = cacheMap.get(id);

            priorityQueue.removeFirstOccurrence(id);
            priorityQueue.addFirst(id);
        }


    }

    public synchronized void prepare(long id) {
        prepare(id, Priority.NORMAL);
    }

    public synchronized void prepare(long id, Priority priority) {
        // clean up cache if necessary
        cacheCleanup();
        if (cacheMap.containsKey(id)) {
            priorityQueue.removeFirstOccurrence(id);
            priorityQueue.addFirst(id);
           CacheEntry entry = cacheMap.get(id);
           if (entry.getStatus() == FileStatus.PREPARING || entry.getStatus() == FileStatus.TRANSCODING || entry.getStatus() == FileStatus.PREPARED) {
               // preparation is already in progress or complete
               logger.info("{} is already prepared or preparing", id);
               return;
           }
        } else {
            priorityQueue.addFirst(id);
        }

        // start or re-start prepare process

        // get neccessary information from track database
        String[] deviceAndPath = TrackDatabase.getInstance().getDeviceAndPath(id);
        if (deviceAndPath == null) {
            logger.warn("prepare() called with unknown track id");
            return;
        }

        // create cache entry
        CacheEntry entry = new CacheEntry(id, FileStatus.PREPARING);
        // set size negative size to indicate that the size is currently unknown
        // the real size ist set in prepareFinished()
        entry.setFileSize(-1);
        cacheMap.put(id, entry);

        // get device
        Device device = DeviceManager.getInstance().getDevice(deviceAndPath[0]);
        //TODO: device.needsPrepare() is ignored!

        // create temporary file
        File downloadFile = new File(downloadDir.getPath() + "/" + id);
        try {
            downloadFile.createNewFile();
        } catch (IOException e) {
            logger.warn("Failed to create temporary download file while preparing " + id);
            entry.setMessage("Failed to create temporary download file.");
            entry.setStatus(FileStatus.FAILED);
            return;
        }

        // submit prepare job
        logger.info("submit prepare job for " + id);
        threadPool.submit(new PrepareJob(priority, id, device, deviceAndPath[1], downloadFile, transcodeCommand));
    }

    public synchronized void cacheCleanup() {
        if (currentCacheSize > maxCacheSize) {
            logger.info("running cache cleanup...");
            int count = 0;
            while(currentCacheSize > maxCacheSize * 0.8) {
                long key = priorityQueue.removeLast();
                File file = new File(cacheDir.getPath() + "/" + key);
                file.delete();
                cacheMap.remove(key);
                count++;
            }
            logger.info("{} files dropped.", count);
        }
    }

    /* package-private callback functions for PrepareJob */

    //TODO: this is a bit not-so elegantly done
    synchronized void prepareComplete(long id) {
        String filename = "" + id;
        File downloadFile = new File(downloadDir.getPath() +"/" + filename);
        if (!cacheMap.containsKey(id)) {
            logger.info("Discarding downloaded file for {}: file dropped from cache index.", id);
            // file was removed from cache during preparation
            // that means it is no longer needed
            // delete downloaded file
            downloadFile.delete();
            return;
        }

        // prepare successful - move the temporary file to the cache directory
        File targetFile = new File(cacheDir.getPath() + "/" + filename);
        downloadFile.renameTo(targetFile);

        // all successful - update cache entry
        cacheMap.get(id).setStatus(FileStatus.PREPARED);
        cacheMap.get(id).setFileSize(targetFile.length());
        // *HACK*: refresh size
        cacheMap.put(id, cacheMap.get(id));
        logger.info("Preparation of {} complete.", id);
        EventBusService.publish(new PrepareCompleteEvent(id));
    }

    synchronized void prepareFailed(long id, String message) {
        // remove the (maybe partially downloaded or empty) file
        String filename = "" + id;
        File downloadFile = new File(downloadDir.getPath() + "/" + filename);
        downloadFile.delete();

        if (cacheMap.containsKey(id)) {
            // update cache entry
            cacheMap.get(id).setStatus(FileStatus.FAILED);
            cacheMap.get(id).setMessage(message);
            EventBusService.publish(new PrepareFailedEvent(id));
        }
    }

    synchronized void transcodeStarted(long id) {
        if (cacheMap.containsKey(id)) {
            cacheMap.get(id).setStatus(FileStatus.TRANSCODING);
        }
    }

    // currently unused, because we cannot determine the file size reliably
    synchronized void setCompletion(long id, float completion) {
        if (cacheMap.containsKey(id)) {
            cacheMap.get(id).setCompletion(completion);
        }
    }

    // access method for TranscodeInputStream
    synchronized InputStream getTranscodeInputStream(long id) throws FileNotFoundException {
        File f = new File(downloadDir.getAbsolutePath() + "/" + id);
        return new FileInputStream(f);
    }
}
