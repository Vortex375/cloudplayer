package de.pandaserv.music.server.misc;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 1/31/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class HttpUtil {
    public static void fail(int statusCode, String message, Request baseRequest, HttpServletResponse response) throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.println(message);
        out.close();
        baseRequest.setHandled(true);
    }
}
