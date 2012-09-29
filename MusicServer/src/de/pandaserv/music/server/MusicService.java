package de.pandaserv.music.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicService extends AbstractHandler {

    static final Logger logger = LoggerFactory.getLogger(MusicService.class);
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        StringBuilder debug = new StringBuilder();
        debug.append("Got request for: ");
        debug.append(target);
        debug.append(request.getMethod());
        debug.append(" ");
        debug.append(request.getContextPath());
        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
            String s = e.nextElement();
            debug.append(s);
            debug.append(": ");
            debug.append(request.getHeader(s));
        }
        logger.debug(debug.toString());
        
        HashMap<String, String> requestContent = 
                new ObjectMapper().readValue(request.getInputStream(), HashMap.class);
        
        
        
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = new PrintWriter(response.getOutputStream());
        if (requestContent.containsKey("name")) {
            out.println("Hello, " + requestContent.get("name") + "!");
        } else {
            out.println("Hello World!");
        }
        out.close();
        baseRequest.setHandled(true);
    }
}
