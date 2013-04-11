package de.pandaserv.music.server.service;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.misc.HttpUtil;
import de.pandaserv.music.server.misc.SessionUtil;
import de.pandaserv.music.shared.FileStatus;
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

        private final long rangeStart;
        private final long rangeEnd;

        private long bytesSent;

        /**
         * Create new a new StreamJob that streams (audio) data to a client.
         * @param streamId streamId - for debug purposes
         * @param client client identifier (e.g. an IP-address) - for debug purposes
         * @param outStream the output stream where the streaming data is written (usually a ServletOutputStream)
         * @param inStream the input stream where the data to be streamed is read from (usually a FileInputStream)
         * @param rangeStart start of the range to stream, in bytes
         * @param rangeEnd end of the range to stream, in bytes
         */
        private StreamJob(long streamId, String client, OutputStream outStream, InputStream inStream, long rangeStart, long rangeEnd) {
            this.streamId = streamId;   // for debug output only
            this.client = client;       // for debug output only
            this.outStream = outStream;
            this.inStream = inStream;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;

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
            long streamLength = rangeEnd - rangeStart + 1;
            int read;
            try {
            logger.info("streaming task started for stream " + streamId);
            long skip = inStream.skip(rangeStart);
            logger.info("skipped " + skip + " bytes from InputStream.");
                read = inStream.read(buf, 0, (int) Math.min(buf.length, streamLength));
                while (read > 0) {
                    streamLength -= read;
                    outStream.write(buf, 0, read);
                    bytesSent += read;
                    read = inStream.read(buf, 0, (int) Math.min(buf.length, streamLength));
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
        if (SessionUtil.getUserId(request.getSession()) < 0) {
            HttpUtil.fail(HttpServletResponse.SC_FORBIDDEN, "You must log in to access this interface.", response);
            return;
        }

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
         * check if the requested file is available from cache
         */
        if (!(CacheManager.getInstance().getStatus(id) == FileStatus.PREPARED || CacheManager.getInstance().getStatus(id) == FileStatus.TRANSCODING)) {
            // stream is not (yet) available
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "No data is available for this stream.", response);
            return;
        }

        /*
         * check if the total size of the requested file is known
         * the size is unknown if the file has not yet finished transcoding
         *
         * if the total size is unknown, check how many bytes are currently available for reading
         */
        long length;
        long available;
        synchronized (CacheManager.getInstance()) {
            length = CacheManager.getInstance().getSize(id);
            if (length < 0) {
                available = CacheManager.getInstance().getInputStream(id).available();
            } else {
                available = length;
            }
        }

        /*
         * check if the request contains a "Range" header field
         * and parse the given range
         */
        long rangeStart;
        long rangeEnd;
        boolean isRangeRequest;
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader == null) {
            /*
             * no range specified? -> send full file
             */
            rangeStart = 0;
            rangeEnd = available - 1;
            isRangeRequest = false;
        } else if (!rangeHeader.startsWith("bytes=")) {
            /*
             * invalid range header - range not specified in bytes
             */
            HttpUtil.fail(HttpServletResponse.SC_BAD_REQUEST, "The only accepted Range is \"byte\"", response);
            return;
        } else {
            /*
             * parse range
             */
            isRangeRequest = true;
            String[] rangeStrings = rangeHeader.substring(6).split("-");
            try {
                rangeStart = Long.parseLong(rangeStrings[0]);
                if (rangeStrings.length < 2 || rangeStrings[1].equals("*")) {
                    /*
                     * range end not specified
                     * set to end of file
                     */
                    rangeEnd = available - 1;
                } else {
                    rangeEnd = Long.parseLong(rangeStrings[1]);
                }
            } catch (NumberFormatException e) {
                HttpUtil.fail(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse Range header.", response);
                return;
            }
        }

        /*
         * check if the parsed ranges are valid
         */
        if (rangeEnd < rangeStart) {
            HttpUtil.fail(HttpServletResponse.SC_BAD_REQUEST, "Invalid ranges specified in request.", response);
            return;
        }
        if (rangeEnd > available) {
            rangeEnd = available - 1;
        }

        /*
         * get InputStream from cache
         */
        InputStream inStream;
        try {
            inStream = CacheManager.getInstance().getInputStream(id);
        } catch (IOException e) {
            logger.error("Unable to get input stream for {}", id);
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Unable to open input stream", response);
            return;
        }

        /*
         * set response status code and header fields
         */
        if (isRangeRequest && length > 0) {
            /*
             * the request contained a "Range" header, so we must reply with "206 PARTIAL CONTENT"
             */
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);
            /*
             * we do not set the content length here
             * this causes jetty to automatically switch to "Transfer-Encoding: chunked"
             * TODO: check if this actually makes a difference
             */

            /*if (length > 0) {
                response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);
            } else {
                response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/*");
            }*/
        } else {
            /*
             * the request did not contain a "Range" header
             * or the total size of the file is unknown
             */
            response.setStatus(HttpServletResponse.SC_OK);
            if (length > 0) {
                /*
                 * specify the content length, if known
                 */
                response.setContentLength((int) length);
            } else {
                /*
                 * do not set content length and use
                 * Transfer-Encoding: chunked to stream the file as it is being generated
                 *
                 * we set rangeEnd to an unspecified large value
                 * so the streaming task will keep running until it hits the end of file
                 */
                rangeEnd = Integer.MAX_VALUE - 1;
            }
        }

        /*
         * set additional header fields
         */
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
                inStream,
                rangeStart,
                rangeEnd);
        streamJob.run();
    }
}
