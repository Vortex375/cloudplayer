package de.pandaserv.music.server.devices;

import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author ich
 */
public interface Device {
    public enum Status {
        ONLINE,
        OFFLINE,
        ERROR
    }
    
    public Status getStatus();
    
    public void getFile(String path, OutputStream out);
    
    public void setup(Properties config);
}
