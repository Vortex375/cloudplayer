package de.pandaserv.music.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.HashAttributeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class MusicService extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        
        System.out.println("Got request for: " + target);
        System.out.println("" + request.getMethod() + " " + request.getContextPath());
        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
            String s = e.nextElement();
            System.out.println(s + ": " + request.getHeader(s));
        }
        
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
