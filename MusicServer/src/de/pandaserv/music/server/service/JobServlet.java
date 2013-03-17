package de.pandaserv.music.server.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.misc.HttpUtil;
import de.pandaserv.music.server.misc.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

class JobServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (SessionUtil.getUserId(request) < 0) {
            HttpUtil.fail(HttpServletResponse.SC_FORBIDDEN, "You must log in to access this interface.", response);
            return;
        }

        if (!request.getPathInfo().equals("/")) {
            // for now
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.println("Accessing individual job properties not implemented yet!");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator jg = jsonFactory.createJsonGenerator(response.getOutputStream());

        jg.writeStartObject();
        Map<Long, Job> jobs = JobManager.getInstance().listJobs();
        for (long key: jobs.keySet()) {
            Job job = jobs.get(key);
            jg.writeFieldName("" + key);
            jg.writeStartObject();
            jg.writeFieldName("description");
            jg.writeString(job.getDescription());
            jg.writeFieldName("status");
            jg.writeString(job.getStatus());
            jg.writeEndObject();
        }
        jg.writeEndObject();
        jg.flush();
        jg.close();
    }

}
