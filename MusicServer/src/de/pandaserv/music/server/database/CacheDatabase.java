package de.pandaserv.music.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CacheDatabase {
    public static class Entry {
        private long id;
        private long available;

        public Entry(long id, long available) {
            this.id = id;
            this.available = available;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getAvailable() {
            return available;
        }

        public void setAvailable(long available) {
            this.available = available;
        }
    }


    static final Logger logger = LoggerFactory.getLogger(CacheDatabase.class);
    private final LocalPreparedStatement putEntry;
    private final LocalPreparedStatement listEntries;
    private final LocalPreparedStatement removeEntry;
    private final LocalPreparedStatement clear;

    // Singleton
    private static final CacheDatabase ourInstance = new CacheDatabase();

    public static CacheDatabase getInstance() {
        return ourInstance;
    }

    private CacheDatabase() {
        putEntry = new LocalPreparedStatement("" +
                "MERGE INTO CacheIndex as index" +
                " USING (VALUES (?, ?))" +
                " AS vals(id, available)" +
                " ON vals.id=index.id" +
                " WHEN MATCHED THEN UPDATE SET index.available=vals.available" +
                " WHEN NOT MATCHED THEN INSERT VALUES vals.id, vals.available");
        listEntries = new LocalPreparedStatement("" +
                "SELECT id, available" +
                " FROM CacheIndex");
        removeEntry = new LocalPreparedStatement("" +
                "DELETE FROM CacheIndex" +
                " WHERE id=?");
        clear = new LocalPreparedStatement("" +
                "DELETE FROM CacheIndex");
    }

    public void putEntry(long id, long available) {
        PreparedStatement ps;

        try {
            ps = putEntry.get();
            ps.setLong(1, id);
            ps.setLong(2, available);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("SQL Exception in putEntry(): {}", e);
        }
    }

    public List<Entry> listEntries() {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = listEntries.get();
            rs = ps.executeQuery();

            List<Entry> ret = new ArrayList<>();
            while (rs.next()) {
                ret.add(new Entry(rs.getLong(1), rs.getLong(2)));
            }
            return ret;
        } catch (SQLException e) {
            logger.warn("SQL Exception in listEntries(): {}", e);
            return null;
        }
    }

    public void removeEntry(long id) {
        PreparedStatement ps;

        try {
            ps = removeEntry.get();
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("SQL Exception in clear(): {}", e);
        }
    }

    public void clear() {
        PreparedStatement ps;

        try {
            ps = clear.get();
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("SQL Exception in clear(): {}", e);
        }
    }
}
