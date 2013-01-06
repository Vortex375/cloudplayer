package de.pandaserv.music.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class TrackDatabase {

    static final Logger logger = LoggerFactory.getLogger(TrackDatabase.class);
    private final LocalPreparedStatement getDeviceAndPath;
    // Singleton
    private static final TrackDatabase ourInstance = new TrackDatabase();

    public static TrackDatabase getInstance() {
        return ourInstance;
    }

    private TrackDatabase() {
        getDeviceAndPath = new LocalPreparedStatement(""
                + "SELECT `device`, `device_path`"
                + " FROM `Devices`"
                + " WHERE `id`=?");
    }

    public String[] getDeviceAndPath(long id) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = getDeviceAndPath.get();
            ps.setLong(1, id);
            rs = ps.executeQuery();

            if(rs.next()) {
                return new String[]{rs.getString("device"), rs.getString("device_path")};
            } else {
                logger.warn("Unknown track id: " + id);
            }
        } catch (SQLException e) {
            logger.warn("SQL Exception in listDeviceNames():");
            e.printStackTrace();
        }

        return null;
    }
}
