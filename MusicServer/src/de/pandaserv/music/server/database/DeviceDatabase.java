package de.pandaserv.music.server.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDatabase {

    static final Logger logger = LoggerFactory.getLogger(DeviceDatabase.class);
    private final LocalPreparedStatement listDevices;
    private final LocalPreparedStatement getDeviceProperties;
    // Singleton
    private static final DeviceDatabase ourInstance = new DeviceDatabase();

    public static DeviceDatabase getInstance() {
        return ourInstance;
    }

    private DeviceDatabase() {
        listDevices = new LocalPreparedStatement(""
                + "SELECT `name`, `type`"
                + " FROM `Devices`");
        getDeviceProperties = new LocalPreparedStatement(""
                + "SELECT `key`, `value`"
                + " FROM `Attributes`"
                + " WHERE `object`=?");
    }

    public List<String[]> listDevices() {
        PreparedStatement ps;
        ResultSet rs;

        List<String[]> ret = new LinkedList<>();

        try {
            ps = listDevices.get();
            rs = ps.executeQuery();

            while (rs.next()) {
                ret.add(new String[]{rs.getString("name"), rs.getString("type")});
            }
        } catch (SQLException e) {
            logger.warn("SQL Exception in listDevices():");
            e.printStackTrace();
        }

        return ret;
    }

    public Properties getDeviceProperties(String deviceName) {
        PreparedStatement ps;
        ResultSet rs;

        Properties ret = new Properties();

        try {
            ps = getDeviceProperties.get();
            ps.setString(1, "device:" + deviceName);
            rs = ps.executeQuery();

            while (rs.next()) {
                String key = rs.getString("key");
                String value = rs.getString("value");

                ret.setProperty(key, value);
            }
        } catch (SQLException e) {
            logger.warn("SQL Exception in getDeviceProperties():");
            e.printStackTrace();
        }

        return ret;
    }
}
