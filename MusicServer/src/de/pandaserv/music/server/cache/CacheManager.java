package de.pandaserv.music.server.cache;

import de.pandaserv.music.server.database.CacheDatabase;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.misc.StringUtil;
import de.pandaserv.music.shared.TrackLength;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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

    public synchronized void shutdown() {
        CacheDatabase.getInstance().clear();
        for (CacheEntry entry: cacheMap.values()) {
            CacheDatabase.getInstance().putEntry(entry.getId(), entry.getAvailable());
        }
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
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        indexCache();
        cacheCleanup();
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
            priorityQueue.addFirst(id);
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

            createCacheFile(id, length.getFileSize());

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

        File entryFile = new File(cacheDir.getPath() + "/" + id);
        PrepareJob prepareJob;
        if (cacheEntry.getAvailable() < cacheEntry.getFileSize()) {
            // prepare job is needed
            // get neccessary information from track database
            String[] deviceAndPath = TrackDatabase.getInstance().getDeviceAndPath(id);
            if (deviceAndPath == null) {
                logger.warn("getInputStream() called with unknown track id");
                return null;
            }
            // get device
            Device device = DeviceManager.getInstance().getDevice(deviceAndPath[0]);
            String path = deviceAndPath[1];

            prepareJob = new PrepareJob(cacheEntry, device, path, entryFile);
        } else {
            // file is already fully cached
            prepareJob = null;
        }

        return new CacheInputStream(cacheEntry,
                prepareJob, entryFile, transcodeCommand, offsetSeconds);
    }

    //TODO: more efficient way to initalize a file to zeroes?
    private void createCacheFile(long id, long length) throws IOException {
        File f = new File(cacheDir.getPath() + "/" + id);

        FileOutputStream out = new FileOutputStream(f);
        byte[] nullBuffer = new byte[1048576]; // 1MB null byte buffer
        while (length > 0) {
            out.write(nullBuffer, 0, (int) Math.min(nullBuffer.length, length));
            length -= nullBuffer.length;
        }
        out.flush();
        out.close();
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
}
