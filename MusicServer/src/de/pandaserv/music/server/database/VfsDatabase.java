package de.pandaserv.music.server.database;

import de.pandaserv.music.shared.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/16/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class VfsDatabase {

    static final Logger logger = LoggerFactory.getLogger(VfsDatabase.class);

    private final LocalPreparedStatement listAllTracks;
    private final LocalPreparedStatement listNewTracks;
    private final LocalPreparedStatement clear;

    private final LocalPreparedStatement getId;
    private final LocalPreparedStatement listDir;
    private final LocalPreparedStatement addEntry;

    // Singleton
    private static final VfsDatabase ourInstance = new VfsDatabase();

    public static VfsDatabase getInstance() {
        return ourInstance;
    }

    private VfsDatabase() {
        listAllTracks = new LocalPreparedStatement("" +
                "SELECT id, device, device_path" +
                " FROM Tracks" +
                " ORDER BY device, device_path");
        listNewTracks = new LocalPreparedStatement("" +
                "SELECT id, device, device_path" +
                " FROM Tracks" +
                " WHERE id NOT IN" +
                "   (SELECT trackId FROM VirtualFS WHERE trackId IS NOT NULL)" +
                " ORDER BY device, device_path");
        clear = new LocalPreparedStatement("" +
                "DELETE FROM VirtualFS");
        getId = new LocalPreparedStatement("" +
                "SELECT id" +
                " FROM VirtualFS" +
                " WHERE device=? AND path=?");
        listDir = new LocalPreparedStatement("" +
                "SELECT id, parent, device, path, trackId" +
                " FROM VirtualFS" +
                " WHERE parent=?");
        addEntry = new LocalPreparedStatement("" +
                "INSERT INTO VirtualFS" +
                " (parent, device, path, trackId)" +
                " VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
    }

    private long mkdirs(String device, String path) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = getId.get();
        ps.setString(1, device);
        ps.setString(2, path);

        rs = ps.executeQuery();

        // check whether directory already exists
        if (rs.next()) {
            // dir already exists
            return rs.getLong(1);
        } else {
            // create new directory
            File f = new File(path);
            String parentDir = f.getParent();
            long parent;
            if (parentDir != null) {
                // recursively create parent dir
                parent = mkdirs(device, parentDir);
            }else {
                // we already are at the top-level dir
                parent = -1;
            }
            ps = addEntry.get();
            if (parent > 0) {
                ps.setLong(1, parent);
            } else {
                // for top-level directories set parent to NULL
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, device);
            ps.setString(3, path);
            ps.setNull(4, Types.BIGINT); // directories have trackId NULL
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        }
    }

    public void rebuild() throws SQLException {
        PreparedStatement ps;

        ps = clear.get();
        ps.executeUpdate();

        update();
    }

    public void update() throws SQLException {
        PreparedStatement ps;
        PreparedStatement add;
        ResultSet rs;

        ps = listNewTracks.get();
        add = addEntry.get();

        rs = ps.executeQuery();

        String currentDevice = "";
        String currentDir = "";
        long currentDirId = -1;
        while (rs.next()) {
            // create new file system entry for track
            long trackId = rs.getLong(1);
            String device = rs.getString(2);
            String path = rs.getString(3);
            File f = new File(path);
            String dir = f.getParent();

            // recursively create parent directories
            if (!device.equals(currentDevice) || !dir.equals(currentDir)) {
                currentDirId = mkdirs(device, dir);
                currentDir = dir;
                currentDevice = device;
            }

            add.setLong(1, currentDirId);
            add.setString(2, device);
            add.setString(3, path);
            add.setLong(4, trackId);
            add.executeUpdate();
        }
    }

    public List<Directory> listDir(long id) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = listDir.get();
        ps.setLong(1, id);

        rs = ps.executeQuery();

        List<Directory> ret = new ArrayList<>();
        while(rs.next()) {
            long nodeId = rs.getLong(1);
            long parent = rs.getLong(2);
            String device = rs.getString(3);
            String path = rs.getString(4);
            long trackId = rs.getLong(5);

            if (rs.wasNull()) {
                // is directory
                ret.add(new Directory(nodeId, device, parent, path));
            } else {
                // is file (track)
                ret.add(new Directory(nodeId, device, parent, path, trackId));
            }
        }

        return ret;
    }

    public List<Directory> listDir(String device, String path) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = getId.get();
        ps.setString(1, device);
        ps.setString(2, path);

        rs = ps.executeQuery();
        if (rs.next()) {
            return listDir(rs.getLong(1));
        } else {
            // dir does not exist
            return null;
        }
    }
}
