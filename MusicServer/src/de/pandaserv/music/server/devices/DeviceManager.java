/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices;

import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.database.DeviceDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ich
 */
public class DeviceManager {
    static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    //TODO: static device configuration
    // map device type -> implementing class

    private static final Map<String, String> DEVICE_TYPES;

    static {
        DEVICE_TYPES = new HashMap<>();
        DEVICE_TYPES.put("ssh", "de.pandaserv.music.server.devices.ssh.SshDevice");
    }
    private Map<String, DeviceHandle> devices;
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
            devices.put(name, new DeviceHandle(name, type));
        }
    }

    /**
     * Get a list of the names of all known devices.
     *
     * @return
     */
    public synchronized List<String> listDevices() {
        List<String> ret = new ArrayList<>(devices.size());

        for (DeviceHandle dev : devices.values()) {
            if (!DEVICE_TYPES.containsKey(dev.type)) {
                logger.warn("The Device {} has an unknown type {}. Ignoring device",
                        new Object[]{dev.name, dev.type});
                continue;
            }
            ret.add(dev.name);
        }

        return ret;
    }

    public synchronized Device getDevice(String name) {
        if (!devices.containsKey(name)) {
            logger.error("Unknown device {} requested with getDevice()", name);
            throw new RuntimeException("Unknown device: " + name);
        }
        
        DeviceHandle handle = devices.get(name);
        
        if (handle.instance == null) {
            // instantiate device object
            Device dev = null;
            try {
                Class<?> cls = Class.forName(DEVICE_TYPES.get(handle.type));
                dev = (Device) cls.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                logger.error("Device instanciation failed:", ex);
            } catch (ClassNotFoundException ex) {
                logger.error("Unable to find class for device type {}", handle.type);
            }
            if (dev == null) {
                throw new RuntimeException("Device instanciation failed");
            }
            // get device config
            Properties deviceConfig = DeviceDatabase.getInstance().getDeviceProperties(name);
            dev.setup(deviceConfig);
            
            handle.instance = dev;
        }
        
        return handle.instance;
    }
}
