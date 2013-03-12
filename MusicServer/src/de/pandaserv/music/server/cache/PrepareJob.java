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
import java.util.Arrays;

/**
 *
 * @author ich
 */
public class PrepareJob implements Job {

    private static final int COPY_BUFFER_SIZE = 1048576; // 1MB

    static final Logger logger = LoggerFactory.getLogger(PrepareJob.class);

    private final long id;
    private final Device device;
    private final String path;
    private final File outputFile;
    private long startTime;
    private String transcodeCommand;

    //private boolean canceled = false;

    public PrepareJob(long id, Device device, String path, File outputFile, String transcodeCommand) {
        this.id = id;
        this.device = device;
        this.path = path;
        this.outputFile = outputFile;
        this.transcodeCommand = transcodeCommand;
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
        //TODO: cannot cancel for now
        //canceled = true;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        logger.info("Downloading and transcoding {} from {} to {}", path, device, outputFile.getAbsolutePath());
        final long jobId = JobManager.getInstance().addJob(this);
        try {
            try {
                // create the transcode command
                String[] args = transcodeCommand.split(" ");
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("%o")) {
                        args[i] = outputFile.getAbsolutePath();
                    }
                }

                ProcessBuilder builder = new ProcessBuilder(Arrays.asList(args));
                builder.redirectInput(ProcessBuilder.Redirect.PIPE);
                //builder.redirectError(ProcessBuilder.Redirect.PIPE);
                //builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                logger.info("Running {}", args);
                Process proc = builder.start();
                OutputStream outStream = proc.getOutputStream();

                // get input stream
                InputStream inStream = device.getFile(path);
                if (inStream == null) {
                    logger.error("Unable to get input stream from device.");
                    CacheManager.getInstance().prepareFailed(id, "Unable to get input stream from device.");
                    return;
                }
                // start copy process
                byte[] buf = new byte[COPY_BUFFER_SIZE];
                int read = inStream.read(buf);
                while (read > 0) {
                    outStream.write(buf, 0, read);
                    read = inStream.read(buf);
                }
                // finish copy
                outStream.flush();
                outStream.close();
                inStream.close();

                // wait for transcode command to finish
                try {
                    logger.info("Waiting for transcode command to finish");
                    int ret = proc.waitFor();
                    if (ret != 0) {
                        logger.warn("Transcode command finished with status code {}", ret);
                        logger.warn("This is the transcode command's stderr output:");
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        String line;
                        while((line = errorReader.readLine()) != null) {
                            logger.warn(line);
                        }
                        CacheManager.getInstance().prepareFailed(id, "The transcode command finished unsuccessfully");
                        return;
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
            } catch (FileNotFoundException e) {
                logger.error("Uh oh. Output file does not exist - it should have been created by CacheManager.");
                CacheManager.getInstance().prepareFailed(id, "Cannot write output file: file not found");
                return;
            } catch (IOException e) {
                logger.error("IOException while writing to output file: " + e.getMessage());
                CacheManager.getInstance().prepareFailed(id, "IOException while writing to output file: " + e.getMessage());
                return;
            }

        logger.info("Preparation completed successfully.");
            CacheManager.getInstance().prepareComplete(id);

        } finally {
            JobManager.getInstance().removeJob(jobId);
            logger.info("PrepareJob finished for {} (took {}ms)", id, (System.currentTimeMillis() - startTime));
        }
    }
}
