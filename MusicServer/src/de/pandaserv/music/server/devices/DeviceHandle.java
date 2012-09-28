package de.pandaserv.music.server.devices;

/**
 * Simple class to store handles to device instances. Used by DeviceManager.
 * @author Benjamin Schmitz
 */
class DeviceHandle {
    public final String name;
    public final String type;
    public Device instance;
    
    public DeviceHandle(String name, String type) {
        this.name = name;
        this.type = type;
        this.instance = null;
    }

    @Override
    public String toString() {
        return "DeviceHandle{" + "name=" + name + ", type=" + type + ", instance=" + instance + '}';
    }
}
