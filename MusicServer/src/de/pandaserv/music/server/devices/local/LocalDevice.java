package de.pandaserv.music.server.devices.local;

import de.pandaserv.music.server.devices.Device;

import java.io.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 1/6/13
 * Time: 3:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalDevice implements Device {
    private String name;

    @Override
    public boolean needsPrepare() {
        return false;
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
        return "local";
    }

    @Override
    public Status getStatus() {
        // local files are always "online"
        return Status.ONLINE;
    }

    @Override
    public String getStatusMessage() {
        return "";
    }

    @Override
    public InputStream getFile(String path) throws FileNotFoundException {
        return new FileInputStream(new File(path));
    }

    @Override
    public void setup(Properties config) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
