package de.pandaserv.music.server.service;

import de.pandaserv.music.server.misc.SessionUtil;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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

        /*
         * handle session-related events
         */
        getSessionHandler().getSessionManager().addEventListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent event) {
                /*
                 * drop saved queries from cache when a user session is destroyed
                 */
                HttpSession session = event.getSession();
                long lastQuery = SessionUtil.getLastTrackQueryId(session);
                if (lastQuery > 0) {
                    RequestCache.getInstance().drop(lastQuery);
                }
            }
        });
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
