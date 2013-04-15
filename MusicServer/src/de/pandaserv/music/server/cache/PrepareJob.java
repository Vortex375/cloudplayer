/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.cache;

import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * @author ich
 */
public class PrepareJob implements Job {

    private static final int COPY_BUFFER_SIZE = 524288; // 512kB

    static final Logger logger = LoggerFactory.getLogger(PrepareJob.class);

    private final CacheEntry cacheEntry;
    private final Device device;
    private final String path;
    private final File outputFile;
    private long startTime;
    private long creationTime;

    private boolean canceled = false;

    private String status;

    private Thread executingThread;

    public PrepareJob(CacheEntry cacheEntry, Device device, String path, File outputFile) {
        this.cacheEntry = cacheEntry;
        this.device = device;
        this.path = path;
        this.outputFile = outputFile;
        creationTime = System.currentTimeMillis();

        status = "not yet started";
    }

    @Override
    public String getDescription() {
        return "Downloading " + path + " from " + device;
    }

    @Override
    public synchronized String getStatus() {
        return status;
    }

    private synchronized void setStatus(String status) {
        this.status = status;
    }

    @Override
    public synchronized void cancel() {
        canceled = true;
        //if (executingThread != null) {
        //    executingThread.interrupt();
        //}
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public void run() {
        executingThread = Thread.currentThread();
        final long jobId = JobManager.getInstance().addJob(this);
        try {
            // prepare job takes the cache entry's write lock for the whole time
            setStatus("waiting for file lock...");
            synchronized (cacheEntry.getWriteLock()) {
                // check if it is still necessary to run this job
                setStatus("setting up...");
                if (cacheEntry.getAvailable() == cacheEntry.getFileSize()) {
                    // this file is already fully cached
                    // nothing to do
                    logger.info("PrepareJob started but file is already fully prepared. Exiting.");
                    return;
                }
                if (isCanceled()) {
                    return;
                }
                startTime = System.currentTimeMillis();
                logger.info("Downloading {} from {} to {}", path, device, outputFile.getAbsolutePath());

                try {
                    // get input stream
                    InputStream inStream = device.getFile(path, cacheEntry.getAvailable());
                    if (inStream == null) {
                        logger.error("Unable to get input stream from device.");
                        return;
                    }

                    // open file for writing
                    // (we should be the only thread writing on this file, since we have the write lock)
                    RandomAccessFile outFile = new RandomAccessFile(outputFile, "rw");
                    outFile.seek(cacheEntry.getAvailable());

                    // start copy process
                    byte[] buf = new byte[COPY_BUFFER_SIZE];
                    int read = inStream.read(buf);
                    double completion;
                    while (!isCanceled() && read > 0) {
                        outFile.write(buf, 0, read);
                        cacheEntry.setAvailable(cacheEntry.getAvailable() + read);
                        completion = (cacheEntry.getAvailable() / (double) cacheEntry.getFileSize());
                        logger.info("Downloading {} ({}%)", cacheEntry.getId(), (int) (completion * 100));
                        setStatus("Downloading (" + (int) (completion * 100) + "%)");
                        read = inStream.read(buf);
                    }

                    if (cacheEntry.getAvailable() == cacheEntry.getFileSize()) {
                        logger.info("File {} is now fully prepared.", cacheEntry.getId());
                    }
                    // finish copy
                    outFile.close();
                    inStream.close();
                } catch (IOException e) {
                    //Thread.interrupted(); // clear interrupt flag
                    logger.error("IOException while downloading file: {}", e);
                }
            }
            logger.info("Prepare job finished normally.");
        } finally {
            JobManager.getInstance().removeJob(jobId);
            logger.info("PrepareJob finished for {} (took {}ms)", cacheEntry.getId(), (System.currentTimeMillis() - startTime));
        }
    }
}
