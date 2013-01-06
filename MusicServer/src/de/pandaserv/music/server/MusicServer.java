/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.service.MusicService;
import de.pandaserv.music.server.service.StreamService;
import de.pandaserv.music.server.ssh.SshPortForwardService;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        DatabaseManager.setup(startupConfig);
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
        ResourceHandler resource = new ResourceHandler();
        logger.info("Using web content path {}", startupConfig.getProperty("web_dir"));
        resource.setResourceBase(startupConfig.getProperty("web_dir"));
        //resource.setWelcomeFiles(new String[]{"MusicWebApp.html"});
        //TODO: for testing only - disable later
        resource.setDirectoriesListed(true);
        resource.setAliases(true);

        // music service
        MusicService service = new MusicService();

        // register handlers and start server
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource, service});

        setHandler(handlers);
    }
}
