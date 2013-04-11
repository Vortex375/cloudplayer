package de.pandaserv.music.server.cache;

import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/27/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class TranscodeDataJob implements Job {
    static final Logger logger = LoggerFactory.getLogger(TranscodeDataJob.class);

    private static final int COPY_BUFFER_SIZE = 256;

    private int byteCount;
    private final InputStream in;
    private final TranscodeInputStream_ out;

    public TranscodeDataJob(InputStream in, TranscodeInputStream_ out) {
        this.in = in;
        this.out = out;
        byteCount = 0;
    }


    @Override
    public String getDescription() {
        return "Copy transcode data.";
    }

    @Override
    public synchronized String getStatus() {
        return String.format("%d bytes copied", byteCount);
    }

    @Override
    public void cancel() {
        // cannot cancel
    }

    @Override
    public void run() {
        final long jobId = JobManager.getInstance().addJob(this);
        logger.info("Starting transcode copy job.");
        try {
            byte[] buf = new byte[COPY_BUFFER_SIZE];
            int read = in.read(buf);
            while (read > 0) {
                synchronized (this) {
                    byteCount += read;
                }
                out.pushData(buf, 0, read);
                read = in.read(buf);
            }
            out.finishWrite();
        } catch (IOException e) {
            logger.error("Transcode copy job interrupted! The file was only partially copied.");
            logger.error("Trace: ", e);
            out.setErrored(true);
            out.finishWrite();
        } finally {
            out.finishWrite();
            logger.info("Bytes copied by TranscodeJob: {}", byteCount);
            JobManager.getInstance().removeJob(jobId);
        }
    }
}
