/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices.ssh;

import de.pandaserv.music.server.devices.Device;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author ich
 */
public class SshDevice implements Device {

    @Override
    public Status getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getStatusMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getFile(String path, OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setup(Properties config) {
        //TODO: implement
    }
    
}
