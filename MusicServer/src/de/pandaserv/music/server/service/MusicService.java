package de.pandaserv.music.server.service;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicService extends ServletContextHandler {

    static final Logger logger = LoggerFactory.getLogger(MusicService.class);

    public MusicService() {
        // enable session support
        super(SESSIONS);

        // stream object
        addServlet(new ServletHolder(new StreamServlet()), "/service/stream/*");
        // cover object
        addServlet(new ServletHolder(new CoverServlet()), "/service/cover/*");
        // job object
        addServlet(new ServletHolder(new JobServlet()), "/service/jobs");
        // GWT interface
        addServlet(new ServletHolder(new GwtMusicServiceImpl()), "/service/gwt");
    }

    /**
     * For debug output only

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
        logger.debug(debug.toString());

        super.handle(target, baseRequest, request, response);
    }*/
}
