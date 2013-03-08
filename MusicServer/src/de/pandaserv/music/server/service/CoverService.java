package de.pandaserv.music.server.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.misc.HttpUtil;
import de.pandaserv.music.shared.Cover;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.StringTokenizer;

class CoverService extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // check if cover was specified
        StringTokenizer tk = new StringTokenizer(target, "/");
        if (!tk.hasMoreTokens()) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Please specify the cover's md5 sum.",
                    baseRequest, response);
            return;
        }

        // get cover
        String md5 = tk.nextToken();
        Cover cover = TrackDatabase.getInstance().getCover(md5);
        if (cover == null) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Unknown cover id.",
                    baseRequest, response);
            return;
        }

        baseRequest.setHandled(true);
        response.setContentType(cover.getMimeType());
        response.setContentLength(cover.getData().length);
        OutputStream out = response.getOutputStream();
        out.write(cover.getData());
        out.flush();
    }
}
