package de.pandaserv.music.server.service;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.cache.FileStatus;
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
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class StreamService extends AbstractHandler {

    private static final int STREAM_BUFFER_SIZE = 5120; // 5kB

    static final Logger logger = LoggerFactory.getLogger(StreamService.class);

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!request.getMethod().equals(HttpMethods.GET)) {
            fail(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid method for stream. The only supported method is GET.",
                    baseRequest, response);
            return;
        }

        // check if stream id was specified
        StringTokenizer tk = new StringTokenizer(target, "/");
        if (!tk.hasMoreTokens()) {
            fail(HttpServletResponse.SC_NOT_FOUND, "Please specify a stream id, such as \"/stream/12345\".",
                    baseRequest, response);
            return;
        }
        String idString = tk.nextToken();


        // parse stream id
        long id;
        try {
            id = Long.parseLong(idString);
        } catch (NumberFormatException e) {
            fail(HttpServletResponse.SC_NOT_FOUND, "Invalid stream id: " + idString,
                    baseRequest, response);
            return;
        }

        // get stream data
        if (CacheManager.getInstance().getStatus(id) != FileStatus.PREPARED) {
            // stream is not (yet) available
            fail(HttpServletResponse.SC_NOT_FOUND, "No data is available for this stream.",
                    baseRequest, response);
            return;
        }

        long length = CacheManager.getInstance().getSize(id);

        // parse requested range
        long rangeStart;
        long rangeEnd;
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader == null) {
            rangeStart = 0;
            rangeEnd = length - 1;
        } else if (!rangeHeader.startsWith("bytes=")) {
            fail(HttpServletResponse.SC_BAD_REQUEST, "The only accepted Range is \"byte\"",
                    baseRequest, response);
            return;
        } else {
            String[] rangeStrings = rangeHeader.substring(6).split("-");
            try {
                rangeStart = Long.parseLong(rangeStrings[0]);
                if (rangeStrings.length < 2 || rangeStrings[1].equals("*")) {
                    rangeEnd = length - 1;
                } else {
                    rangeEnd = Long.parseLong(rangeStrings[1]);
                }
            } catch (NumberFormatException e) {
                fail(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse Range header.",
                        baseRequest, response);
                return;
            }
        }

        // check for valid ranges
        if (rangeEnd < rangeStart) {
            fail(HttpServletResponse.SC_BAD_REQUEST, "Invalid ranges specified in request.",
                    baseRequest, response);
            return;
        }
        if (rangeEnd > length) {
            rangeEnd = length - 1;
        }

        InputStream inStream = CacheManager.getInstance().getInputStream(id);
        OutputStream outStream = response.getOutputStream();


        // setHeaders
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "audio/mpeg"); //TODO: detect/retrieve format!!
        response.setHeader("Content-Length", "" + (rangeEnd - rangeStart + 1));
        response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);
        baseRequest.setHandled(true);

        // begin streaming task
        byte[] buf = new byte[STREAM_BUFFER_SIZE];
        long streamLength = rangeEnd - rangeStart + 1;
        int read;
        logger.info("streaming task started for stream " + id);
        long skip = inStream.skip(rangeStart);
        logger.info("Skipped " + skip + " bytes from InputStream.");
        try {
            do {
                read = inStream.read(buf, 0, (int) Math.min(buf.length, streamLength));
                streamLength -= read;
                //rangeStart += read; // for debug output
                logger.info("sending " +  read + " bytes (" + streamLength + " remaining)");
                outStream.write(buf, 0, read);
            } while (read == buf.length);
            outStream.flush();
        } finally {
            outStream.close();
            inStream.close();
            logger.info("streaming task completed for stream " + id);
        }
    }

    private void fail(int statusCode, String message, Request baseRequest, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.println(message);
        out.close();
        baseRequest.setHandled(true);
    }
}
