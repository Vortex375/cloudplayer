/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 *
 * @author ich
 */
public class MusicServer extends Server {
    public MusicServer(int port) throws IOException {
        super(port);

        Properties startupConfig = new Properties();
        InputStream configIn = getClass().getClassLoader().getResourceAsStream("startup.cfg");
        if (configIn == null) {
            System.err.println("FATAL: unable to locate startup configuration file 'startup.cfg'");
            System.exit(1);
        }
        startupConfig.load(configIn);
        configIn.close();
        
        // static web app content
        ResourceHandler resource = new ResourceHandler();
        Logger.getLogger(MusicServer.class.getName()).log(Level.INFO,
                "Using web content path {0}", startupConfig.getProperty("web_dir"));
        resource.setResourceBase(startupConfig.getProperty("web_dir"));
        resource.setWelcomeFiles(new String[]{"MusicWebApp.html"});

        // fake service
        ContextHandler context = new ContextHandler();
        context.setContextPath("/service");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(new MusicService());

        // register handlers and start server
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource, context});

        setHandler(handlers);
    }
}
