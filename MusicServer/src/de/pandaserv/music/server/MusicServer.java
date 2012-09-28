/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server;

import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.devices.DeviceManager;
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
    public MusicServer(Properties startupConfig) throws IOException {
        super(Integer.parseInt(startupConfig.getProperty("port")));
        
        // set up database
        DatabaseManager.setup(startupConfig);
        
        // set up remote device manager
        DeviceManager.setup();
        
        //DEBUG: test device
        Device testDev = DeviceManager.getInstance().getDevice("testdevice");
        System.out.println("Test device:" + testDev);
        
        // static web app content
        ResourceHandler resource = new ResourceHandler();
        Logger.getLogger(MusicServer.class.getName()).log(Level.INFO,
                "Using web content path {0}", startupConfig.getProperty("web_dir"));
        resource.setResourceBase(startupConfig.getProperty("web_dir"));
        resource.setWelcomeFiles(new String[]{"MusicWebApp.html"});

        // music service
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
