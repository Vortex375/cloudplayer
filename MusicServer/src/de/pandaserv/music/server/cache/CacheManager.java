package de.pandaserv.music.server.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ich
 */
public class CacheManager {
    static Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private Map<Integer, CacheEntry> cacheMap;
    private File cacheDir;
    private File downloadDir;
    
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
        
        // prepare cache directory
        String cachePath = config.getProperty("cache_dir");
        cacheDir = new File(cachePath);
        
        downloadDir = new File(cachePath + "/download");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        
        // index existing files in cache directory
        logger.info("Indexing cache...");
        for (File f: cacheDir.listFiles()) {
            if (!f.isFile()) {
                continue;
            }
            String filename = f.getName();
            
            try {
                int id = Integer.parseInt(filename);
                cacheMap.put(id, new CacheEntry(id, f.getAbsolutePath(), FileStatus.PREPARED));
            } catch (NumberFormatException e) {
                logger.warn("My cache directory contains a strange file: {}", filename);
            }
        }
        logger.info("{} files indexed", cacheMap.size());
    }
    
    public synchronized FileStatus getStatus(int id) {
        if (!cacheMap.containsKey(id)) {
            return FileStatus.NOT_PREPARED;
        } else {
            return cacheMap.get(id).getStatus();
        }
    }
    
    public synchronized InputStream getInputStream(int id) {
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
    
    public void prepare(int id) {
        if (cacheMap.containsKey(id)) {
            // already prepared or preparing
            return;
        }
        
        //TODO
    }
}
