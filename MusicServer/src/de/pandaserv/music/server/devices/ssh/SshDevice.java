/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices.ssh;

import com.jcraft.jsch.*;
import de.pandaserv.music.server.devices.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private JSch jsch;
    private Session session;

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
    public InputStream getFile(String path) throws IOException {
        return getFile(path, 0);
    }

    @Override
    public InputStream getFile(String path, long offset) {
        if (!connected || !session.isConnected()) {
            try {
                connect();
            } catch (JSchException e) {
                logger.error("Failed to connect to ssh device.");
                logger.error("Trace: ", e);
                status = Status.ERROR;
                statusMessage = e.getMessage();
                return null;
            }
        }
        try {
            String command="dd bs=1 skip="+offset+" if=\"" + path + "\"";
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            return channel.getInputStream();
        } catch (JSchException | IOException e) {
            logger.error("Failed to download file from ssh device.");
            logger.error("Trace:", e);
            return null;
        }
    }

    @Override
    public void setup(Properties config) {
        host = config.getProperty("ssh-host");
        port = Integer.parseInt(config.getProperty("ssh-port"));
        username = config.getProperty("ssh-username");
        password = config.getProperty("ssh-password");

        jsch = new JSch();
    }

    @Override
    public void shutdown() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            session = null;
        }
    }

    private void connect() throws JSchException {
        session = jsch.getSession(username, host, port);

        // disable host key checking
        //TODO: maybe not the best thing to do?
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.setPassword(password);
        session.connect();
        connected = true;
    }

}
