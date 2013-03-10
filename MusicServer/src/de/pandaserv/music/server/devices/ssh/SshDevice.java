/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices.ssh;

import de.pandaserv.music.server.devices.Device;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.ConnectFuture;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author ich
 */
public class SshDevice implements Device {

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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return "ssh";
    }

    @Override
    public Status getStatus() {
        return status;
    }
    
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public InputStream getFile(String path) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    }

}
