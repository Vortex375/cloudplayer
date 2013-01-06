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

    static final Logger logger = LoggerFactory.getLogger(PrepareJob.class);

    private final long id;
    private final Device device;
    private final String path;
    private final File outputFile;

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
    public float getCompletion() {
        return 0;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void run() {
        final long jobId = JobManager.getInstance().addJob(this);
        try {
            OutputStream oStream = null;
            try {
                oStream = new FileOutputStream(outputFile);
                device.getFile(path, oStream);
                oStream.flush();
                oStream.close();
            } catch (FileNotFoundException e) {
                logger.error("Uh oh. Output file does not exist - it should have been created by CacheManager.");
                CacheManager.getInstance().prepareFailed(id, "Cannot write output file: file not found");
                return;
            } catch (IOException e) {
                logger.error("IOException while writing to output file: " + e.getMessage());
                CacheManager.getInstance().prepareFailed(id, "IOException while writing to output file: " + e.getMessage());
                return;
            }

            CacheManager.getInstance().prepareComplete(id);

        } finally {
            JobManager.getInstance().removeJob(jobId);
        }
    }
}
