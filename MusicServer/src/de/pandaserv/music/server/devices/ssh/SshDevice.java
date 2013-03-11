/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices.ssh;

import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.server.ssh.AuthenticationException;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author ich
 */
public class SshDevice implements Device {

    static final Logger logger = LoggerFactory.getLogger(SshDevice.class);

    private String name;
    private Status status;
    private String statusMessage;

    private boolean connected;
    private String host;
    private int port;
    private String username;
    private String password;

    private SshClient client;
    private ClientSession session;

    public SshDevice() {
        connected = false;
        status = Status.OFFLINE;
        statusMessage = "";
    }

    @Override
    public boolean needsPrepare() {
        return true;
    }

    @Override
    public synchronized String getName() {
        return name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return "ssh";
    }

    @Override
    public synchronized Status getStatus() {
        return status;
    }
    
    @Override
    public synchronized String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public InputStream getFile(String path) {
        if (!connected) {
            try {
                connect();
                connected = true;
            } catch (Exception e) {
                logger.error("Failed to connect to ssh device.");
                logger.error("Trace: ", e);
                status = Status.ERROR;
                statusMessage = e.getMessage();
                return null;
            }
        }

        try {
            ChannelExec channel = session.createExecChannel("cat " + path);
            return channel.getIn();
        } catch (Exception e) {
            logger.error("Failed to retrieve file from ssh device.");
            logger.error("Trace: ", e);
            return null;
        }
    }

    @Override
    public void setup(Properties config) {
        host = config.getProperty("ssh-host");
        port = Integer.parseInt(config.getProperty("ssh-port"));
        username = config.getProperty("ssh-username");
        password = config.getProperty("ssh-password");

        client = SshClient.setUpDefaultClient();
    }

    private void connect() throws Exception {
        ConnectFuture cf = client.connect(host, port);
        cf.await();
        ClientSession session = cf.getSession();

        AuthFuture auth = session.authPassword(username, password);
        auth.await();
        if (!auth.isSuccess()) {
            throw new AuthenticationException("Invalid username or password");
        }
    }

}
