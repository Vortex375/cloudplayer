package de.pandaserv.music.server.service;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.misc.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class StreamServlet extends HttpServlet {

    private static final int STREAM_BUFFER_SIZE = 5120; // 5kB

    static final Logger logger = LoggerFactory.getLogger(StreamServlet.class);

    private static class StreamJob implements Job {

        private final long streamId; // for debug output only
        private final String client; // ditto

        private final OutputStream outStream;
        private final InputStream inStream;

        private long bytesSent;

        /**
         * Create new a new StreamJob that streams (audio) data to a client.
         * @param streamId streamId - for debug purposes
         * @param client client identifier (e.g. an IP-address) - for debug purposes
         * @param outStream the output stream where the streaming data is written (usually a ServletOutputStream)
         * @param inStream the input stream where the data to be streamed is read from (usually a CacheInputStream)
         */
        private StreamJob(long streamId, String client, OutputStream outStream, InputStream inStream) {
            this.streamId = streamId;   // for debug output only
            this.client = client;       // for debug output only
            this.outStream = outStream;
            this.inStream = inStream;

            bytesSent = 0;              // for debug output only
        }

        @Override
        public String getDescription() {
            return "Streaming task for " + streamId + " (to " + client + ")";
        }

        @Override
        public String getStatus() {
            return bytesSent + " bytes sent";
        }

        @Override
        public void cancel() {
            // cannot cancel
        }

        @Override
        public void run() {
            final long jobId = JobManager.getInstance().addJob(this);

            byte[] buf = new byte[STREAM_BUFFER_SIZE];
            int read;
            try {
            logger.info("streaming task started for stream " + streamId);
                read = inStream.read(buf, 0, buf.length);
                while (read > 0) {
                    outStream.write(buf, 0, read);
                    bytesSent += read;
                    read = inStream.read(buf, 0, buf.length);
                }
                outStream.flush();
            } catch (IOException e) {
                logger.info("streaming task interrupted: " + e.toString());
            } finally {
                JobManager.getInstance().removeJob(jobId);
                try {
                    inStream.close();
                } catch (IOException e) {
                    logger.warn("failed to close input stream after streaming task ended");
                    // ignore
                }
                logger.info("streaming task ended for stream " + streamId);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * check access privileges
         */
//        if (SessionUtil.getUserId(request.getSession()) < 0) {
//            HttpUtil.fail(HttpServletResponse.SC_FORBIDDEN, "You must log in to access this interface.", response);
//            return;
//        }

        /*
         * check if stream id was specified
         */
        StringTokenizer tk = new StringTokenizer(request.getPathInfo(), "/");
        if (!tk.hasMoreTokens()) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Please specify a stream id, such as \"/stream/12345\".",
                    response);
            return;
        }

        /*
         * parse stream id
         */
        String idString = tk.nextToken();
        long id;
        try {
            id = Long.parseLong(idString);
        } catch (NumberFormatException e) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Invalid stream id: " + idString, response);
            return;
        }

        /*
         * parse stream offset
         */
        String offsetString;
        int offset;
        if ((offsetString = request.getHeader("X-Stream-Offset-Seconds")) != null) {
            try {
                offset = Integer.parseInt(offsetString);
            } catch (NumberFormatException e) {
                logger.warn("Invalid stream offset header value: {}", offsetString);
                offset = 0;
            }
        } else {
            offset = 0;
        }

        /*
         * get InputStream from cache
         */
        InputStream inStream;
        try {
            inStream = CacheManager.getInstance().getInputStream(id, offset);
        } catch (IOException e) {
            logger.error("Unable to get input stream for {}", id);
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Unable to open input stream", response);
            return;
        }

        /*
         * set header fields and status
         */
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("audio/webm");
        response.setHeader("Accept-Ranges", "bytes");

        /*
         * get the name (ip-adress) of the client that issued the request
         * this is only for debug purposes
         */
        String client = request.getHeader("X-Forwarded-For");
        if (client == null) {
            client = request.getRemoteAddr();
        }

        /*
         * begin the streaming task
         */
        OutputStream outStream = response.getOutputStream();
        StreamJob streamJob = new StreamJob(
                id,         // for debug
                client,     // for debug
                outStream,
                inStream);
        streamJob.run();
    }
}
