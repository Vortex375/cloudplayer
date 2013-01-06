package de.pandaserv.music.server.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicService extends ContextHandlerCollection {

    static final Logger logger = LoggerFactory.getLogger(MusicService.class);

    public MusicService() {
        ContextHandler context;

        // stream object
        context = new ContextHandler();
        context.setContextPath("/service/stream");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(new StreamService());
        addHandler(context);
    }

    /**
     * For debug output only
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (!target.startsWith("/service")) {
            // only handle requests to "/service/*"
            return;
        }
        StringBuilder debug = new StringBuilder();
        debug.append("Got request for: ");
        debug.append(target);
        debug.append(" ");
        debug.append(request.getMethod());
        debug.append(" ");
        debug.append(request.getContextPath());
        debug.append(" ");
        debug.append("\n");
        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
            String s = e.nextElement();
            debug.append(s);
            debug.append(": ");
            debug.append(request.getHeader(s));
            debug.append("\n");
        }
        logger.info(debug.toString());

        super.handle(target, baseRequest, request, response);
    }
}
