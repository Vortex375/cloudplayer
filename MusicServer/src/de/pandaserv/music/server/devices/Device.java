package de.pandaserv.music.server.devices;

import java.io.IOException;
import java.io.InputStream;
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

    public boolean needsPrepare(); //TODO: this is currently unused (can be removed?)

    public String getName();

    public void setName(String name);

    public String getType();

    public Status getStatus();
    
    public String getStatusMessage();
    
    public InputStream getFile(String path) throws IOException;
    
    public void setup(Properties config);

    public void shutdown();
}
