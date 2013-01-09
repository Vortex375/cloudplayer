package de.pandaserv.music.server.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

class JobService extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!target.equals("/")) {
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
