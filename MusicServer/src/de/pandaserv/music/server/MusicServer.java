/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.service.GwtMusicServiceImpl;
import de.pandaserv.music.server.service.MusicService;
import de.pandaserv.music.server.ssh.SshPortForwardService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;

/**
 *
 * @author ich
 */
public class MusicServer extends Server {
    static final Logger logger = LoggerFactory.getLogger(MusicServer.class);
    
    public MusicServer(Properties startupConfig) throws IOException {
        super(Integer.parseInt(startupConfig.getProperty("port")));

        // set up job manager
        JobManager.setup();
        // set up database
        try {
            DatabaseManager.setup(startupConfig);
        } catch (SQLException e) {
            logger.error("Unable to setup database:");
            e.printStackTrace();
            System.exit(1);
        }
        // set up remote device manager
        DeviceManager.setup();
        // set up cache manager
        CacheManager.setup(startupConfig);


        // set up ssh forwarding service
        int sshPort = Integer.parseInt(startupConfig.getProperty("ssh_port"));
        if (sshPort > 0) {
            SshPortForwardService serv = SshPortForwardService.setup(sshPort);
        }
        
        // set up http server
        
        // static web app content
        /*ResourceHandler resource = new ResourceHandler();
        logger.info("Using web content path {}", startupConfig.getProperty("web_dir"));
        resource.setResourceBase(startupConfig.getProperty("web_dir"));
        //resource.setWelcomeFiles(new String[]{"MusicWebApp.html"});
        //TODO: for testing only - disable later
        resource.setDirectoriesListed(true);
        resource.setAliases(true);*/

        ServletContextHandler staticContent = new ServletContextHandler();
        staticContent.setContextPath("/");
        staticContent.setResourceBase(startupConfig.getProperty("web_dir"));
        ServletHolder staticContentHolder = new ServletHolder(new DefaultServlet());
        staticContent.addFilter(new FilterHolder(new CacheFilter()), "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        staticContent.addServlet(staticContentHolder, "/");

        // music service
        MusicService service = new MusicService();
        // gwt servlet
        ServletContextHandler gwtContext = new ServletContextHandler();
        // set resrouce base to web_dir, so the gwt servlet can access the serialization policy file
        gwtContext.setResourceBase(startupConfig.getProperty("web_dir"));
        gwtContext.setContextPath("/");
        ServletHolder gwtServletHolder = new ServletHolder(new GwtMusicServiceImpl());
        gwtContext.addServlet(gwtServletHolder, "/service/gwt");

        // register handlers and start server
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{gwtContext, service, staticContent});

        setHandler(handlers);
    }
}
