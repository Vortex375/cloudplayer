package de.pandaserv.music.server.cache;

import com.adamtaft.eb.EventBusService;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.events.PrepareCompleteEvent;
import de.pandaserv.music.server.events.PrepareFailedEvent;
import de.pandaserv.music.server.misc.StringUtil;
import de.pandaserv.music.shared.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
            if (containsKey(key)) {
                // remove size of previous entry
                currentCacheSize -= get(key).getSize();
            }
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

    private static final int COPY_BUFFER_SIZE = 8192; // used in prepareFinished()

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

        // prepare at most three files simultaneously
        //TODO: make configurable
        executorService = Executors.newFixedThreadPool(3);

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
            TranscodeInputStream ret = entry.getTranscodeInputStream();
            ret.rewind(); // make sure the returned input stream is positioned at 0
            return ret;
        } else {
            throw new RuntimeException("The requested file has not finished preparing.");
        }
    }
    
    public synchronized void prepare(long id) {
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

    private void transcode(File in, TranscodeInputStream out) throws IOException, InterruptedException {
        //String cmd = transcodeCommand.replace("%i", "\"" + in.getAbsolutePath() + "\"")
        //        .replace("%o", "\"" + out.getAbsolutePath() + "\"");
        //String cmd = transcodeCommand.replace("%i", "\"" + in.getAbsolutePath() + "\"");
        String[] args = transcodeCommand.split(" ");
        for (int i = 0; i < args.length; i++) {
            // find the input file argument
            if (args[i].equals("%i")) {
                args[i] = in.getAbsolutePath();
            }
        }

        ProcessBuilder builder = new ProcessBuilder(Arrays.asList(args));
        builder.redirectError(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        logger.info("Running {} {}", transcodeCommand, args);
        Process proc = builder.start();

        InputStream inStream = proc.getInputStream();
        //Thread copyThread = new Thread(new TranscodeDataJob(inStream, out));
        //copyThread.start();
        TranscodeDataJob copyJob = new TranscodeDataJob(inStream, out);
        copyJob.run();
        logger.info("Waiting for transcode command to finish");
<<<<<<< HEAD
        proc.waitFor();
        //logger.info("Waiting for copy job to finish");
        //copyThread.join();
=======
        int ret = proc.waitFor();
        if (ret != 0) {
            logger.warn("Transcode command finished with status code {}", ret);
            logger.warn("This is the transcode command's stderr output:");
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while((line = errorReader.readLine()) != null){
                logger.warn(line);
            }
        }

        logger.info("Waiting for copy job to finish");
        copyThread.join();
>>>>>>> 19ff4aa51e50dea225995296ca00ed5f2b36543c
    }

    /* package-private callback functions for PrepareJob */

    //TODO: this is a bit not-so elegantly done
    void prepareComplete(long id) {
        String filename = "" + id;
        File downloadFile = new File(downloadDir.getPath() +"/" + filename);
        TranscodeInputStream transcodeInputStream;
        synchronized (this) {
            if (!cacheMap.containsKey(id)) {
                logger.info("Discarding downloaded file for {}: file dropped from cache index.", id);
                // file was removed from cache during preparation
                // that means it is no longer needed
                // delete downloaded file
                downloadFile.delete();
                return;
            }
            // else
            cacheMap.get(id).setStatus(FileStatus.TRANSCODING);
            cacheMap.get(id).setSize(-1); // indicate unfinished file
            transcodeInputStream = new TranscodeInputStream();
            cacheMap.get(id).setTranscodeInputStream(transcodeInputStream);

        } // release object lock during transcode

        logger.info("Starting transcode for {}", id);
        try {
            transcode(downloadFile, transcodeInputStream);
        } catch (IOException | InterruptedException e) {
            // transcode failed -> call prepareFailed()
            logger.error("Error during transcoding of file {}", id);
            logger.error("Trace: ", e);
            prepareFailed(id, "Error during transcoding of file.");
            return;
        }
        if (transcodeInputStream.isErrored()) {
            logger.error("Error during transcoding of file {}", id);
            logger.error("The copy job failed.");
            prepareFailed(id, "Error during transcoding of file.");
            return;
        }
        logger.info("Transcode finished for {}", id);

        synchronized (this) {
            // transcode finished - copy the _transcoded data to the target file
            // and delete the downloaded file
            File targetFile = new File(cacheDir.getPath() + "/" + filename);
            byte[] data = transcodeInputStream.getData();
            try {
                targetFile.createNewFile();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
                out.write(data);
                out.flush();
                out.close();
            } catch (IOException e) {
                logger.error("Error during transcoding of file {}", id);
                logger.error("Unable to write to the target cache file: ", e);
                prepareFailed(id, "Unable to write on target file");
                return;
            } finally {
                downloadFile.delete();
            }

            // all successful - update cache entry
            cacheMap.get(id).setStatus(FileStatus.PREPARED);
            cacheMap.get(id).setSize(data.length);
            cacheMap.get(id).setTranscodeInputStream(null); // clean up input stream and temporary cached data
            // *HACK*: refresh size
            cacheMap.put(id, cacheMap.get(id));
            logger.info("Preparation of {} complete.", id);
        }
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
}
