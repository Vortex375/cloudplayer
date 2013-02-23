package de.pandaserv.music.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class AttributesDatabase {

    static final Logger logger = LoggerFactory.getLogger(AttributesDatabase.class);
    private final LocalPreparedStatement getAllAttributes;
    private final LocalPreparedStatement getAttribute;
    private final LocalPreparedStatement setAttribute;
    // Singleton
    private static final AttributesDatabase ourInstance = new AttributesDatabase();

    public static AttributesDatabase getInstance() {
        return ourInstance;
    }

    private AttributesDatabase() {
        getAttribute= new LocalPreparedStatement("" +
                "SELECT value" +
                " FROM Attributes" +
                " WHERE object=? AND key=?");
        getAllAttributes = new LocalPreparedStatement("" +
                "SELECT key, value" +
                " FROM Attributes" +
                " WHERE object=?");
        setAttribute = new LocalPreparedStatement("" +
                "MERGE INTO Attributes AS a" +
                " USING (VALUES(?, ?, ?))" +
                " AS vals(object, key, value)" +
                " ON vals.object = a.object AND vals.key = a.key" +
                " WHEN MATCHED THEN UPDATE SET a.value = vals.value" +
                " WHEN NOT MATCHED THEN INSERT (a.object, a.key, a.value) VALUES (vals.object, vals.key, vals.value)");
    }

    public String getAttribute(String object, String key) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = getAttribute.get();
            ps.setString(1, object);
            ps.setString(2, key);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }

        } catch (SQLException e) {
            logger.warn("SQL Exception in getAttribute():");
            e.printStackTrace();
            return null;
        }
    }

    public Properties getAllAttributes(String object) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            Properties ret = new Properties();
            ps = getAllAttributes.get();
            ps.setString(1, object);
            rs = ps.executeQuery();

            boolean found = false;
            while(rs.next()) {
                found = true;
                ret.setProperty(rs.getString(1), rs.getString(2));
            }
            if (found) {
                return ret;
            } else {
                return null;
            }

        } catch (SQLException e) {
            logger.warn("SQL Exception in getAllAttributes():");
            e.printStackTrace();
            return null;
        }
    }

    public void setAttribute(String object, String key, String value) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = setAttribute.get();
            ps.setString(1, object);
            ps.setString(2, key);
            ps.setString(3, value);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.warn("SQL Exception in setAttribute():");
            e.printStackTrace();
        }
    }
}
