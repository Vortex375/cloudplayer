/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices.ssh;

import de.pandaserv.music.server.devices.Device;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author ich
 */
public class SshDevice implements Device {

    private String name;

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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getStatusMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getFile(String path) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setup(Properties config) {
        //TODO: implement
    }
    
}
