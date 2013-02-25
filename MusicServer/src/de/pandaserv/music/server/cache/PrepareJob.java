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

import java.io.*;

/**
 *
 * @author ich
 */
public class PrepareJob implements Job {

    private static final int COPY_BUFFER_SIZE = 8192;

    static final Logger logger = LoggerFactory.getLogger(PrepareJob.class);

    private final long id;
    private final Device device;
    private final String path;
    private final File outputFile;
    private long startTime;

    private boolean canceled = false;

    public PrepareJob(long id, Device device, String path, File outputFile) {
        this.id = id;
        this.device = device;
        this.path = path;
        this.outputFile = outputFile;
    }

    @Override
    public String getDescription() {
        return "Downloading " + path + " from " + device;
    }

    @Override
    public synchronized String getStatus() {
        return "running";
    }

    @Override
    public synchronized void cancel() {
        canceled = true;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        logger.info("Downloading {} from {} to {}", path, device, outputFile.getAbsolutePath());
        final long jobId = JobManager.getInstance().addJob(this);
        try {
            try {
                OutputStream outStream = new FileOutputStream(outputFile);
                InputStream inStream = device.getFile(path);
                byte[] buf = new byte[COPY_BUFFER_SIZE];
                int read;
                do {
                    if (canceled) {
                        outStream.close();
                        inStream.close();
                        CacheManager.getInstance().prepareFailed(id, "Job canceled by user.");
                        return;
                    }
                    read = inStream.read(buf);
                    outStream.write(buf, 0, read);
                } while (read == buf.length);
                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                logger.error("Uh oh. Output file does not exist - it should have been created by CacheManager.");
                CacheManager.getInstance().prepareFailed(id, "Cannot write output file: file not found");
                return;
            } catch (IOException e) {
                logger.error("IOException while writing to output file: " + e.getMessage());
                CacheManager.getInstance().prepareFailed(id, "IOException while writing to output file: " + e.getMessage());
                return;
            }

            logger.info("Download completed sucessfully.");
            CacheManager.getInstance().prepareComplete(id);

        } finally {
            JobManager.getInstance().removeJob(jobId);
            logger.info("PrepareJob finished for {} (took {}ms)", id, (System.currentTimeMillis() - startTime));
        }
    }
}
