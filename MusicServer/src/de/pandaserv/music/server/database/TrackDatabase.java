package de.pandaserv.music.server.database;

import de.pandaserv.music.server.data.Track;
import de.pandaserv.music.server.data.TrackDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class TrackDatabase {

    static final Logger logger = LoggerFactory.getLogger(TrackDatabase.class);

    private final LocalPreparedStatement getDeviceAndPath;
    private final LocalPreparedStatement listTracks;
    private final LocalPreparedStatement getTrackInfo;

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
        listTracks = new LocalPreparedStatement(""
                + "SELECT `id`, `title`, `artist`, `album`"
                + " FROM `Tracks`");
        getTrackInfo = new LocalPreparedStatement(""
                + "SELECT `id`, `device`, `title`, `artist`, `album`, `genre`, `track`, `year`, `device_path`"
                + " FROM `Tracks`"
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
}
