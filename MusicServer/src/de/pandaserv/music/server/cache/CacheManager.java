package de.pandaserv.music.server.cache;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import com.adamtaft.eb.EventBusService;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.events.PrepareCompleteEvent;
import de.pandaserv.music.server.events.PrepareFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ich
 */
public class CacheManager {
    static Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private Map<Long, CacheEntry> cacheMap;
    private File cacheDir;
    private File downloadDir;
    private ExecutorService executorService;
    
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
        cacheMap = new HashMap<>();

        // prepare at most three files simultaneously
        //TODO: make configurable
        executorService = Executors.newFixedThreadPool(3);

        // prepare cache directory
        String cachePath = config.getProperty("cache_dir");
        cacheDir = new File(cachePath);
        
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
        
        // index existing files in cache directory
        logger.info("Indexing cache...");
        for (File f: cacheDir.listFiles()) {
            if (!f.isFile()) {
                continue;
            }
            String filename = f.getName();
            
            try {
                long id = Long.parseLong(filename);
                cacheMap.put(id, new CacheEntry(id, FileStatus.PREPARED));
            } catch (NumberFormatException e) {
                logger.warn("My cache directory contains a strange file: {}", filename);
            }
        }
        logger.info("{} files indexed", cacheMap.size());
    }
    
    public synchronized FileStatus getStatus(long id) {
        if (!cacheMap.containsKey(id)) {
            return FileStatus.NOT_PREPARED;
        } else {
            return cacheMap.get(id).getStatus();
        }
    }
    
    public synchronized InputStream getInputStream(long id) {
        if (!cacheMap.containsKey(id)) {
            throw new RuntimeException("You must call prepare() first, before requesting a file from cache!");
        }
        CacheEntry entry = cacheMap.get(id);
        if (entry.getStatus() != FileStatus.PREPARED) {
            throw new RuntimeException("The requested file has not finished preparing.");
        }
        File f = new File(cacheDir.getAbsolutePath() + "/" + id);
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("FATAL: file indexed in cache but does not exist on disk: " + f.getAbsolutePath());
        }
    }
    
    public void prepare(long id) {
        if (cacheMap.containsKey(id)) {
           CacheEntry entry = cacheMap.get(id);
           if (entry.getStatus() == FileStatus.PREPARING || entry.getStatus() == FileStatus.PREPARED) {
               // preparation is already in-progress or complete
               return;
           }
        }
        
        // start or re-start prepare process

        // get neccessary information from track database
        String[] deviceAndPath = TrackDatabase.getInstance().getDeviceAndPath(id);
        if (deviceAndPath == null) {
            logger.warn("prepare() called with unknown track id");
            return;
        }

        // get device
        Device device = DeviceManager.getInstance().getDevice(deviceAndPath[0]);
        if (!device.needsPrepare()) {
            // this device can stream files directly and does not need preparation
            CacheEntry entry = new CacheEntry(id, FileStatus.PREPARED);
            cacheMap.put(id, entry);
            EventBusService.publish(new PrepareCompleteEvent(id));
            return;
        }

        // prepare (download) the file

        // create or overwrite entry in cache map
        CacheEntry entry = new CacheEntry(id, FileStatus.PREPARING);
        cacheMap.put(id, entry);

        // create temporary file
        File downloadFile = new File(downloadDir.getPath() + id);
        try {
            downloadFile.createNewFile();
        } catch (IOException e) {
            logger.warn("Failed to create temporary download file while preparing " + id);
            entry.setMessage("Failed to create temporary download file.");
            entry.setStatus(FileStatus.FAILED);
            return;
        }

        // submit prepare job
        executorService.submit(new PrepareJob(id, device, deviceAndPath[1], downloadFile));
    }

    /* package-private functions for PrepareJob */

    synchronized void prepareComplete(long id) {
        String filename = "" + id;
        File downloadFile = new File(downloadDir.getPath() + filename);

        if (!cacheMap.containsKey(id)) {
            // file was removed from cache during preparation
            // that means it is no longer needed
            // delete downloaded file
            downloadFile.delete();
        } else {
            // move completed file from download directory to cache directory
            File targetFile = new File(cacheDir.getPath() + filename);
            downloadFile.renameTo(targetFile);

            // update cache entry
            cacheMap.get(id).setStatus(FileStatus.PREPARED);
            EventBusService.publish(new PrepareCompleteEvent(id));
        }
    }

    synchronized void prepareFailed(long id, String message) {
        // remove the (maybe partially downloaded or empty) file
        String filename = "" + id;
        File downloadFile = new File(downloadDir.getPath() + filename);
        downloadFile.delete();

        if (cacheMap.containsKey(id)) {
            // update cache entry
            cacheMap.get(id).setStatus(FileStatus.FAILED);
            cacheMap.get(id).setMessage(message);
            EventBusService.publish(new PrepareFailedEvent(id));
        }
    }
}
