/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.ssh;

import de.pandaserv.music.server.database.DeviceDatabase;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 *
 * @author ich
 */
class DeviceForwardingFilter implements ForwardingFilter {
    Logger logger = LoggerFactory.getLogger(DeviceForwardingFilter.class);

    @Override
    public boolean canForwardAgent(ServerSession ss) {
        return false;
    }

    @Override
    public boolean canForwardX11(ServerSession ss) {
        return false;
    }

    @Override
    public boolean canListen(InetSocketAddress isa, ServerSession ss) {
        logger.info("Authenticating canListen request for {} to address {}",
                new Object[]{ss.getUsername(), isa});
        
        // check if this device is allowed to forward this port
        Properties deviceConfig = DeviceDatabase.getInstance().getDeviceProperties(ss.getUsername());
        
        if (deviceConfig.containsKey("ssh-forward-port")) {
            int forwardPort = Integer.parseInt(deviceConfig.getProperty("ssh-forward-port"));
            if (forwardPort == isa.getPort()) {
                logger.info("Port forward request authenticated for {}", ss.getUsername());
                return true;
            } else {
                logger.info("Port forward request denied for {}: "
                        + "Requested port {} is not the port that was configured for this device ({})",
                        ss.getUsername(), isa.getPort(), forwardPort);
                return false;
            }
        } else {
            logger.info("Port forward request denied for {}: "
                    + "This device is not allowed to forward ports",
                    ss.getUsername());
            return false;
        }
    }

    @Override
    public boolean canConnect(InetSocketAddress isa, ServerSession ss) {
        logger.info("Authenticating canConnect request for {} to address {}",
                new Object[]{isa, ss.getUsername()});
        return false;
    }
    
}
