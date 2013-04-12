package de.pandaserv.music.server.cache;

import com.adamtaft.eb.EventBusService;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.events.PrepareCompleteEvent;
import de.pandaserv.music.server.events.PrepareFailedEvent;
import de.pandaserv.music.server.misc.StringUtil;
import de.pandaserv.music.shared.FileStatus;
import de.pandaserv.music.shared.Priority;
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
public class CacheManager_ {
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

    private class JobComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable o1, Runnable o2) {
            if (o1 instanceof PrepareJob && o2 instanceof PrepareJob) {
                PrepareJob job1 = (PrepareJob) o1;
                PrepareJob job2 = (PrepareJob) o2;

                int prio1 = 0;
                int prio2 = 0;
                switch(job1.getPriority()) {
                    case HIGH:
                        prio1 = 2;
                        break;
                    case NORMAL:
                        prio1 = 1;
                        break;
                    case LOW:
                        prio1 = 0;
                        break;
                }
                switch(job2.getPriority()) {
                    case HIGH:
                        prio2 = 2;
                        break;
                    case NORMAL:
                        prio2 = 1;
                        break;
                    case LOW:
                        prio2 = 0;
                        break;
                }
                if (prio1 != prio2) {
                    return (prio1 > prio2 ? 1 : -1);
                } else {
                    return new Long(job1.getCreationTime()).compareTo(job2.getCreationTime());
                }
            } else {
                // can't compare those
                return 0;
            }
        }
    }

    static Logger logger = LoggerFactory.getLogger(CacheManager_.class);

    private static final int COPY_BUFFER_SIZE = 8192; // used in prepareFinished()

    private CacheMap cacheMap;
    private long maxCacheSize;
    private long currentCacheSize;
    private Deque<Long> priorityQueue;
    private File cacheDir;
    private File downloadDir;
    private ThreadPoolExecutor threadPool;
    private String transcodeCommand;

    // Singleton
    private static CacheManager_ ourInstance;
    public static CacheManager_ getInstance() {
        return ourInstance;
    }

    public static CacheManager_ setup(Properties config) {
        if (ourInstance != null) {
            logger.warn("CacheManager.setup() called but there is already an instance!");
        } else {
            ourInstance = new CacheManager_(config);
        }

        return ourInstance;
    }

    private CacheManager_(Properties config) {
        cacheMap = new CacheMap();
        currentCacheSize = 0;
        maxCacheSize = StringUtil.parseSize(config.getProperty("cache_max_size"));
        logger.info("max cache size: {}", maxCacheSize);
        priorityQueue = new LinkedList<>();
        transcodeCommand = config.getProperty("transcode_cmd");

        // prepare at most three files simultaneously
        //TODO: make configurable
        threadPool = new ThreadPoolExecutor(3, 3, 0, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(11, new JobComparator()));

        // prepare cache directory
        String cachePath = config.getProperty("cache_dir");
        cacheDir = new File(cachePath);

        // prepare download directory
        downloadDir = new File(cachePath + "/download");
        if (!downloadDir.exists()) {
            // create download dir if it does not exist already
            downloadDir.mkdirs();
        } else {
            // clean up partial downloads
            for (File file: downloadDir.listFiles()) {
                file.delete();
            }
        }
        
        indexCache();
    }

    private void indexCache() {
        // index existing files in cache directory
        logger.info("Indexing cache...");
        for (File f: cacheDir.listFiles()) {
            if (!f.isFile()) {
                continue;
            }
            String filename = f.getName();

            try {
                long id = Long.parseLong(filename);
                CacheEntry entry = new CacheEntry(id, FileStatus.PREPARED);
                entry.setFileSize(f.length());
                cacheMap.put(id, entry);
                priorityQueue.addLast(id);
            } catch (NumberFormatException e) {
                logger.warn("My cache directory contains a strange file: {}", filename);
            }
        }
        logger.info("{} files indexed", cacheMap.size());
        cacheCleanup();
    }
    
    public synchronized FileStatus getStatus(long id) {
        if (!cacheMap.containsKey(id)) {
            return FileStatus.NOT_PREPARED;
        } else {
            return cacheMap.get(id).getStatus();
        }
    }

    /**
     * Get the progress of the running PrepareJob for the file specified by id.
     * This is only meaningful if the file is in state PREPARING or TRANSCODING.
     */
    public synchronized float getCompletion(long id) {
        if (cacheMap.containsKey(id)) {
            return cacheMap.get(id).getCompletion();
        } else {
            return 0;
        }
    }

    public synchronized long getSize(long id) {
        if (!cacheMap.containsKey(id)) {
            return -1;
        } else {
            return cacheMap.get(id).getFileSize();
        }
    }
    
    public synchronized InputStream getInputStream(long id) throws IOException {
        if (!cacheMap.containsKey(id)) {
            throw new RuntimeException("You must call prepare() first, before requesting a file from cache!");
        }

        CacheEntry entry = cacheMap.get(id);

        if (entry.getStatus() == FileStatus.PREPARED) {
            try {
                /*// try direct access
                //TODO: unused
                InputStream ret;
                ret = entry.getDirectAccess();
                if (ret != null) {
                    return ret;
                }*/

                // use file from cache
                File f = new File(cacheDir.getAbsolutePath() + "/" + id);
                return new FileInputStream(f);
            } catch (IOException e) {
                logger.error("IOException while opening input stream for " + id);
                // invalidate the cache entry for this file
                entry.setStatus(FileStatus.FAILED);
                throw e;
            }
        } else if (entry.getStatus() == FileStatus.TRANSCODING) {
            // use the partially downloaded file
            //File f = new File(downloadDir.getAbsolutePath() + "/" + id);
            //return new FileInputStream(f);
            return new TranscodeInputStream(id);
        } else {
            throw new RuntimeException("The requested file has not finished preparing.");
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
