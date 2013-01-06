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

    private static final int STREAM_BUFFER_SIZE = 256;

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

        InputStream inStream = CacheManager.getInstance().getInputStream(id);
        OutputStream outStream = response.getOutputStream();

        // begin streaming
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "audio/mpeg"); //TODO: detect/retrieve format!!
        response.setHeader("Content-Length", "" + CacheManager.getInstance().getSize(id));
        baseRequest.setHandled(true);
        //TODO: how to do this properly?
        byte[] buf = new byte[STREAM_BUFFER_SIZE];
        int read;
        logger.info("streaming task started for stream " + id);
        try {
            do {
                read = inStream.read(buf);
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
