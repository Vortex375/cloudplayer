/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices;

import de.pandaserv.music.server.database.DeviceDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author ich
 */
public class DeviceManager {
    static Logger logger = LoggerFactory.getLogger(DeviceManager.class);
    
    //TODO: static device configuration
    // map device type -> implementing class

    private static final Map<String, String> DEVICE_TYPES;

    static {
        DEVICE_TYPES = new HashMap<>();
        DEVICE_TYPES.put("local", "de.pandaserv.music.server.devices.local.LocalDevice");
        DEVICE_TYPES.put("ssh", "de.pandaserv.music.server.devices.ssh.SshDevice");
    }
    private Map<String, Device> devices;
    // Singleton
    private static DeviceManager ourInstance;

    public static DeviceManager getInstance() {
        return ourInstance;
    }

    public static DeviceManager setup() {
        if (ourInstance != null) {
            logger.warn("DeviceManager.setup() called but there is already an instance!");
        } else {
            ourInstance = new DeviceManager();
        }

        return ourInstance;
    }

    public void shutdown() {
        logger.info("Running shutdown hook: shutting down devices.");
        for (Device d: devices.values()) {
            d.shutdown();
        }
    }

    private DeviceManager() {
        devices = new HashMap<>();

        loadDevices();
        logger.info("Loaded {} devices from database.", devices.size());
    }

    /**
     * Load an initial list of devices from the database.
     */
    private void loadDevices() {
        for (String[] dev : DeviceDatabase.getInstance().listDevices()) {
            String name = dev[0];
            String type = dev[1];
            devices.put(name, createDevice(name, type));
        }
    }

    /**
     * Get a list of all known devices.
     *
     * @return
     */
    public synchronized List<Device> getDevices() {
        return new ArrayList<>(devices.values());
    }

    public synchronized Device getDevice(String name) {
        if (!devices.containsKey(name)) {
            logger.error("Unknown device {} requested with getDevice()", name);
            return null;
        }
        
        return devices.get(name);
    }

    private Device createDevice(String name, String type) {
        // instantiate device object
        Device dev = null;
        try {
            Class<?> cls = Class.forName(DEVICE_TYPES.get(type));
            dev = (Device) cls.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error("Device instanciation failed:", ex);
        } catch (ClassNotFoundException ex) {
            logger.error("Unable to find class for device type {}", type);
        }
        if (dev == null) {
            throw new RuntimeException("Device instantiation failed");
        }
        // get device config
        Properties deviceConfig = DeviceDatabase.getInstance().getDeviceProperties(name);
        dev.setName(name);
        dev.setup(deviceConfig);

        return dev;
    }
}
