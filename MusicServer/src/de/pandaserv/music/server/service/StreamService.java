package de.pandaserv.music.server.service;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.cache.FileStatus;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.misc.HttpUtil;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class StreamService extends AbstractHandler {

    private static final int STREAM_BUFFER_SIZE = 5120; // 5kB

    static final Logger logger = LoggerFactory.getLogger(StreamService.class);

    private static class StreamJob implements Job {

        private final long streamId; // for debug output only
        private final String client; // ditto

        private final OutputStream outStream;
        private final InputStream inStream;

        private final long rangeStart;
        private final long rangeEnd;

        private long bytesSent;

        /**
         * Create new a new Job that streams (audio) data to a client.
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
                do {
                    read = inStream.read(buf, 0, (int) Math.min(buf.length, streamLength));
                    streamLength -= read;
                    //rangeStart += read; // for debug output
                    //logger.info("sending " +  read + " bytes (" + streamLength + " remaining)");
                    outStream.write(buf, 0, read);
                    bytesSent += read;
                } while (read == buf.length);
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
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!request.getMethod().equals(HttpMethods.GET)) {
            HttpUtil.fail(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid method for stream. The only supported method is GET.",
                    baseRequest, response);
            return;
        }

        // check if stream id was specified
        StringTokenizer tk = new StringTokenizer(target, "/");
        if (!tk.hasMoreTokens()) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Please specify a stream id, such as \"/stream/12345\".",
                    baseRequest, response);
            return;
        }

        // parse stream id
        String idString = tk.nextToken();
        long id;
        try {
            id = Long.parseLong(idString);
        } catch (NumberFormatException e) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Invalid stream id: " + idString,
                    baseRequest, response);
            return;
        }

        // get stream data
        if (CacheManager.getInstance().getStatus(id) != FileStatus.PREPARED) {
            // stream is not (yet) available
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "No data is available for this stream.",
                    baseRequest, response);
            return;
        }


        // parse requested range
        long length = CacheManager.getInstance().getSize(id);
        long rangeStart;
        long rangeEnd;
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader == null) {
            // no range specified? -> send full file
            rangeStart = 0;
            rangeEnd = length - 1;
        } else if (!rangeHeader.startsWith("bytes=")) {
            // range not specified in bytes
            HttpUtil.fail(HttpServletResponse.SC_BAD_REQUEST, "The only accepted Range is \"byte\"",
                    baseRequest, response);
            return;
        } else {
            String[] rangeStrings = rangeHeader.substring(6).split("-");
            try {
                rangeStart = Long.parseLong(rangeStrings[0]);
                if (rangeStrings.length < 2 || rangeStrings[1].equals("*")) {
                    // range end not specified
                    // set to end of file
                    rangeEnd = length - 1;
                } else {
                    rangeEnd = Long.parseLong(rangeStrings[1]);
                }
            } catch (NumberFormatException e) {
                HttpUtil.fail(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse Range header.",
                        baseRequest, response);
                return;
            }
        }

        // check for valid ranges
        if (rangeEnd < rangeStart) {
            HttpUtil.fail(HttpServletResponse.SC_BAD_REQUEST, "Invalid ranges specified in request.",
                    baseRequest, response);
            return;
        }
        if (rangeEnd > length) {
            rangeEnd = length - 1;
        }

        InputStream inStream;
        try {
            inStream = CacheManager.getInstance().getInputStream(id);
        } catch (IOException e) {
            logger.error("Unable to get input stream for {}", id);
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Unable to open input stream", baseRequest, response);
            return;
        }
        OutputStream outStream = response.getOutputStream();

        // setHeaders
        if (rangeStart != 0 || rangeEnd != length - 1) {
            // if the parsed range does not cover the entire file
            // set response code to PARTIAL CONTENT
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "audio/webm"); //TODO: always webm!!
        response.setHeader("Content-Length", "" + (rangeEnd - rangeStart + 1));
        response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);
        baseRequest.setHandled(true);

        // begin streaming task

        // get name of client (for debug) and also respect proxy requests
        // only for debug purposes
        String client = request.getHeader("X-Forwarded-For");
        if (client == null) {
            client = request.getRemoteAddr();
        }
        StreamJob streamJob = new StreamJob(id, client,
                                            outStream, inStream,
                                            rangeStart, rangeEnd);
        streamJob.run();
    }
}
