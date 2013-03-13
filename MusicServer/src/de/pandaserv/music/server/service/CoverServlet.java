package de.pandaserv.music.server.service;

import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.misc.HttpUtil;
import de.pandaserv.music.shared.Cover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

class CoverServlet extends HttpServlet {

    static final Logger logger = LoggerFactory.getLogger(CoverServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO: testing only
        Object userId = request.getSession().getAttribute("test-userid");
        logger.info("Test user id from session: {}", userId);

        // check if cover was specified
        StringTokenizer tk = new StringTokenizer(request.getPathInfo(), "/");
        if (!tk.hasMoreTokens()) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Please specify the cover's md5 sum.", response);
            return;
        }

        // get cover
        String md5 = tk.nextToken();
        Cover cover = TrackDatabase.getInstance().getCover(md5);
        if (cover == null) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Unknown cover id.", response);
            return;
        }

        response.setContentType(cover.getMimeType());
        response.setContentLength(cover.getData().length);
        // covers are identified by their md5 sum, so they can be cached forever
        response.setHeader("Cache-Control", "max-age=31556926");
        OutputStream out = response.getOutputStream();
        out.write(cover.getData());
        out.flush();
    }
}
