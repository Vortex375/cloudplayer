/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.ssh;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.DefaultForwardingAcceptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Service to allow remote devices to forward ports to the local machine.
 * 
 * @author ich
 */
public class SshPortForwardService {
    static Logger logger = LoggerFactory.getLogger(SshPortForwardService.class);
    
    private SshServer sshd;
    
    // Singleton
    private static SshPortForwardService ourInstance;

    public static SshPortForwardService getInstance() {
        return ourInstance;
    }
    
    public static SshPortForwardService setup(int port) throws IOException {
        if (ourInstance != null) {
            logger.warn("SshService.setup() called but there is already an instance!");
        } else {
            ourInstance = new SshPortForwardService(port);
        }
        
        return ourInstance;
    }
    
    private SshPortForwardService(int port) throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        //TODO: store host key in another location
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        
        sshd.setForwardingFilter(new DeviceForwardingFilter());
        
        sshd.setTcpipForwardNioSocketAcceptorFactory(new DefaultForwardingAcceptorFactory());
        sshd.setShellFactory(new Factory<Command>() {

            @Override
            public Command create() {
                return new DeviceShell();
            }
        });
        
        sshd.setPublickeyAuthenticator(new DeviceAuthenticator());
        
        sshd.start();
        logger.info("Started SSH port forwarding service at port {}", port);
    }
}
