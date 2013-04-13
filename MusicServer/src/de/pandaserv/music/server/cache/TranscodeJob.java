package de.pandaserv.music.server.cache;

import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/13/13
 * Time: 9:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class TranscodeJob implements Job {

    private static final int COPY_BUFFER_SIZE = 524288; // 512kB

    static Logger logger = LoggerFactory.getLogger(TranscodeJob.class);

    private long id;
    private InputStream inStream;
    private String transcodeCommand;
    private int seekSeconds;
    private boolean canceled = false;

    private Process proc;
    private OutputStream procOutput;

    public TranscodeJob(long id, InputStream inStream, String transcodeCommand, int seekSeconds) {
        this.id = id;
        this.inStream = inStream;
        this.transcodeCommand = transcodeCommand;
        this.seekSeconds = seekSeconds;
    }

    public InputStream setup() throws IOException {
        /*
         * create the transcode command
         * substitute the seek offset parameter
         */
        String[] args = transcodeCommand.split(" ");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("%s")) {
                args[i] = "" + seekSeconds;
            }
        }
        logger.info("Running {}", Arrays.toString(args));

        ProcessBuilder builder = new ProcessBuilder(Arrays.asList(args));
        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        //builder.redirectError(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        proc = builder.start();
        procOutput = proc.getOutputStream();

        return proc.getInputStream();
    }

    @Override
    public String getDescription() {
        return "Transcode Job for " + id;
    }

    @Override
    public String getStatus() {
        return "running";
    }

    @Override
    public synchronized void cancel() {
        canceled = true;
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }

    @Override
    public void run() {
        final long jobId = JobManager.getInstance().addJob(this);
        logger.info("Starting transcode job for stream {}", id);
        try {
            // start copy process
            byte[] buf = new byte[COPY_BUFFER_SIZE];
            int read = inStream.read(buf);
            while (!canceled && read > 0) {
                procOutput.write(buf, 0, read);
                read = inStream.read(buf);
            }

            // finish copy
            procOutput.flush();
            procOutput.close();
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
                    logger.error("The transcode command failed for stream {}", id);
                }
            } catch (InterruptedException e) {
                // ignore
            }
        } catch (IOException e) {
            logger.error("Transcode job for stream {} interrupted by IOException.", id);
            logger.error("Trace: {}", e);
        } finally {
            JobManager.getInstance().removeJob(jobId);
        }
    }
}
