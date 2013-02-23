package de.pandaserv.music.server.database;

import de.pandaserv.music.server.data.Track;
import de.pandaserv.music.server.data.TrackDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class TrackDatabase {

    static final Logger logger = LoggerFactory.getLogger(TrackDatabase.class);

    private final LocalPreparedStatement getDeviceAndPath;
    private final LocalPreparedStatement listTracks;
    private final LocalPreparedStatement getTrackInfo;
    private final LocalPreparedStatement insertTrack;
    private final LocalPreparedStatement updateTrack;
    private final LocalPreparedStatement insertCover;
    private final LocalPreparedStatement cleanupCovers;

    // Singleton
    private static final TrackDatabase ourInstance = new TrackDatabase();

    public static TrackDatabase getInstance() {
        return ourInstance;
    }

    private TrackDatabase() {
        getDeviceAndPath = new LocalPreparedStatement(""
                + "SELECT device, device_path"
                + " FROM Tracks"
                + " WHERE id=?");
        listTracks = new LocalPreparedStatement(""
                + "SELECT id, title, artist, album"
                + " FROM Tracks");
        getTrackInfo = new LocalPreparedStatement(""
                + "SELECT id, device, title, artist, album, genre, track, year, device_path"
                + " FROM Tracks"
                + " WHERE id=?");
        insertTrack = new LocalPreparedStatement("" +
                "INSERT INTO Tracks" +
                " (device, device_id, title, artist, album, genre, track, year, cover, device_path, lastmodified)" +
                " VALUES" +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        updateTrack = new LocalPreparedStatement("" +
                "UPDATE Tracks SET" +
                " title=?," +
                " artist=?," +
                " album=?," +
                " genre=?," +
                " track=?," +
                " year=?," +
                " cover=?," +
                " device_path=?," +
                " lastmodified=?" +
                " WHERE id=?");
        insertCover = new LocalPreparedStatement("" +
                "INSERT INTO Covers" +
                " (md5, data, length, mimetype)" +
                " VALUES" +
                " (?, ?, ?, ?)");
        cleanupCovers = new LocalPreparedStatement("" +
                "DELETE FROM Covers" +
                " WHERE md5 NOT IN (" +
                "   SELECT DISTINCT cover FROM Tracks" +
                "   )");
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

    public List<TrackDetail> listTracks() {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = listTracks.get();
            rs = ps.executeQuery();

            LinkedList<TrackDetail> ret = new LinkedList<>();

            while(rs.next()) {
                long id = rs.getLong(1);
                String title = rs.getString(2);
                String artist = rs.getString(3);
                String album = rs.getString(4);
                ret.add(new TrackDetail(id, title, artist, album));
            }

            return ret;
        } catch (SQLException e) {
            logger.warn("SQL Exception in listDeviceNames():");
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    public Track getTrackInfo(long id) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = listTracks.get();
            ps.setLong(1, id);
            rs = ps.executeQuery();

            if(rs.next()) {
                Track track = new Track();
                track.setId(rs.getLong(1));
                track.setDevice(rs.getString(2));
                track.setTitle(rs.getString(3));
                track.setArtist(rs.getString(4));
                track.setAlbum(rs.getString(5));
                track.setGenre(rs.getString(6));
                track.setTrack(rs.getInt(7));
                track.setYear(rs.getInt(8));
                track.setDevice(rs.getString(9));
                return track;
            } else {
                return null;
            }
        } catch (SQLException e) {
            logger.warn("SQL Exception in listDeviceNames():");
            e.printStackTrace();
            return null;
        }
    }

    public void insertTrack(String device, long device_id, String title, String artist, String album,
                            String genre, int track, int year, String cover, String device_path, Date lastmodified) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = insertTrack.get();
            ps.setString(1, device);
            ps.setLong(2, device_id);
            ps.setString(3, title);
            ps.setString(4, artist);
            ps.setString(5, album);
            ps.setString(6, genre);
            ps.setInt(7, track);
            ps.setInt(8, year);
            ps.setString(9, cover);
            ps.setString(10, device_path);
            ps.setDate(11, lastmodified);

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("SQL Exception in insertTrack():");
            e.printStackTrace();
        }
    }

    public void updateTrack(long id, String title, String artist, String album,
                            String genre, int track, int year, String cover, String device_path, Date lastmodified) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = updateTrack.get();
            ps.setString(1, title);
            ps.setString(2, artist);
            ps.setString(3, album);
            ps.setString(4, genre);
            ps.setInt(5, track);
            ps.setInt(6, year);
            ps.setString(7, cover);
            ps.setString(8, device_path);
            ps.setDate(9, lastmodified);
            ps.setLong(10, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("SQL Exception in updateTrack():");
            e.printStackTrace();
        }
    }

    public void insertCover(String md5, byte[] data, int length, String mimetype) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = insertCover.get();
            ps.setString(1, md5);
            ps.setBytes(2, data);
            ps.setInt(3, length);
            ps.setString(4, mimetype);

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("SQL Exception in insertCover():");
            e.printStackTrace();
        }
    }
}
