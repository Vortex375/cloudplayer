package de.pandaserv.music.server.cache;

import com.adamtaft.eb.EventBusService;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.events.PrepareCompleteEvent;
import de.pandaserv.music.server.events.PrepareFailedEvent;
import de.pandaserv.music.server.misc.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ich
 */
public class CacheManager {
    private class CacheMap extends HashMap<Long, CacheEntry> {
        @Override
        public CacheEntry put(Long key, CacheEntry value) {
            currentCacheSize += value.getSize();
            return super.put(key, value);
        }

        @Override
        public CacheEntry remove(Object key) {
            if (containsKey(key)) {
                CacheEntry entry = get(key);
                currentCacheSize -= entry.getSize();
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
    private File downloadDir;
    private ExecutorService executorService;
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

        // prepare at most five files simultaneously
        //TODO: make configurable
        executorService = Executors.newFixedThreadPool(5);

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
                entry.setSize(f.length());
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

    public synchronized long getSize(long id) {
        if (!cacheMap.containsKey(id)) {
            return -1;
        } else {
            return cacheMap.get(id).getSize();
        }
    }
    
    public synchronized InputStream getInputStream(long id) throws IOException {
        if (!cacheMap.containsKey(id)) {
            throw new RuntimeException("You must call prepare() first, before requesting a file from cache!");
        }

        CacheEntry entry = cacheMap.get(id);

        if (entry.getStatus() != FileStatus.PREPARED) {
            throw new RuntimeException("The requested file has not finished preparing.");
        }


        try {
            // try direct access
            //TODO: unused
            InputStream ret;
            ret = entry.getDirectAccess();
            if (ret != null) {
                return ret;
            }

            // use file from cache
            File f = new File(cacheDir.getAbsolutePath() + "/" + id);
            return new FileInputStream(f);
        } catch (IOException e) {
            logger.error("IOException while opening input stream for " + id);
            throw e;
        }
    }
    
    public void prepare(long id) {
        if (cacheMap.containsKey(id)) {
            priorityQueue.removeFirstOccurrence(id);
            priorityQueue.addFirst(id);
           CacheEntry entry = cacheMap.get(id);
           if (entry.getStatus() == FileStatus.PREPARING || entry.getStatus() == FileStatus.PREPARED) {
               // preparation is already in-progress or complete
               logger.info("{} is already prepared", id);
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

        // create normal cache entry
        CacheEntry entry = new CacheEntry(id, FileStatus.PREPARING);
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
        executorService.submit(new PrepareJob(id, device, deviceAndPath[1], downloadFile));
    }

    public void cacheCleanup() {
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

    private void transcode(File in, File out) throws IOException, InterruptedException {
        String cmd = transcodeCommand.replace("%i", "\"" + in.getAbsolutePath() + "\"")
                .replace("%o", "\"" + out.getAbsolutePath() + "\"");
        logger.info("Running {}", cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        proc.waitFor();
    }

    /* package-private callback functions for PrepareJob */

    //TODO: this keeps a lock on the CacheManager during transcoding which is not good!
    synchronized void prepareComplete(long id) {
        String filename = "" + id;
        File downloadFile = new File(downloadDir.getPath() +"/" + filename);

        if (!cacheMap.containsKey(id)) {
            logger.info("Discarding downloaded file for {}: file dropped from cache index.", id);
            // file was removed from cache during preparation
            // that means it is no longer needed
            // delete downloaded file
            downloadFile.delete();
        } else {
            // transcode the downloaded file to a temporary file
            File transcodeFile = new File(downloadDir.getPath()  + "/" + filename + "_transcode");
            logger.info("Starting transcode for {}", id);
            try {
                transcode(downloadFile, transcodeFile);
            } catch (IOException | InterruptedException e) {
                // transcode failed -> call prepareFailed()
                logger.error("Error during transcoding of file " + id);
                logger.error("Trace: ", e);
                transcodeFile.delete();
                prepareFailed(id, "Error during transcoding of file.");
                return;
            }
            logger.info("Transcode finished for {}", id);
            // transcode finished - move the _transcode file to the target dir
            // and delete the downloaded file
            File targetFile = new File(cacheDir.getPath() + "/" + filename);
            transcodeFile.renameTo(targetFile);
            downloadFile.delete();

            // update cache entry
            cacheMap.get(id).setStatus(FileStatus.PREPARED);
            cacheMap.get(id).setSize(targetFile.length());
            logger.info("Preparation of {} complete.", id);
            EventBusService.publish(new PrepareCompleteEvent(id));
        }
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
}
