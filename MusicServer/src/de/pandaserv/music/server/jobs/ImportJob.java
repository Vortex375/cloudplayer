package de.pandaserv.music.server.jobs;

import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.database.TrackDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/22/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportJob implements Job {
    static final Logger logger = LoggerFactory.getLogger(ImportJob.class);

    private final String device;
    private final String importTableSuffix;
    private boolean cancelled;

    private static final String SELECT_NEW_TRACKS = "" +
            "SELECT id, title, artist, album, genre, track, year, cover, path, lastmodified" +
            " FROM import_tracks_%s" +
            " WHERE id NOT IN (" +
            "   SELECT device_id FROM Tracks WHERE device=?" +
            " )";
    private static final String SELECT_CHANGED_TRACKS = "" +
            "SELECT t.id, i.title, i.artist, i.album, i.genre, i.track, i.year, i.cover, i.path, i.lastmodified" +
            " FROM import_tracks_%s AS i" +
            " JOIN Tracks AS t ON t.device_id=i.id" +
            " WHERE t.device=? AND (i.lastmodified > t.lastmodified)";

    private static final String SELECT_NEW_COVERS = "" +
            "SELECT md5, data, length, mimetype" +
            " FROM import_covers_%s" +
            " WHERE md5 NOT IN (" +
            "   SELECT md5 FROM Covers" +
            " )";

    private static final String DROP_TRACKS_TABLE = "" +
            "DROP TABLE import_tracks_%s";
    private static final String DROP_COVERS_TABLE = "" +
            "DROP TABLE import_covers_%s";

    public ImportJob(String device, String importTableSuffix) {
        this.device = device;
        this.importTableSuffix = importTableSuffix;
        cancelled = false;
    }

    @Override
    public String getDescription() {
        return "Import job " + importTableSuffix;
    }

    @Override
    public synchronized String getStatus() {
        //TODO: more verbose...
        return "running...";
    }

    @Override
    public synchronized void cancel() {
        cancelled = true;
    }

    @Override
    public void run() {
        final long jobId = JobManager.getInstance().addJob(this);
        Connection conn = null;
        try {
            TrackDatabase db = TrackDatabase.getInstance();
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            PreparedStatement selectNewTracksStmt = conn.prepareStatement(String.format(SELECT_NEW_TRACKS,
                    importTableSuffix));
            PreparedStatement selectChangedTracksStmt = conn.prepareStatement(String.format(SELECT_CHANGED_TRACKS,
                    importTableSuffix));
            PreparedStatement selectNewCoversStmt = conn.prepareStatement(String.format(SELECT_NEW_COVERS,
                    importTableSuffix));
            ResultSet rs;

            selectNewTracksStmt.setString(1, device);
            rs = selectNewTracksStmt.executeQuery();
            logger.info("Importing new tracks...");
            while (rs.next()) {
                long id = rs.getLong(1);
                String title = rs.getString(2);
                String artist = rs.getString(3);
                String album = rs.getString(4);
                String genre = rs.getString(5);
                int track = rs.getInt(6);
                int year =  rs.getInt(7);
                String cover = rs.getString(8);
                String path = rs.getString(9);
                Date lmod = rs.getDate(10);

                db.insertTrack(device, id, title, artist, album, genre, track, year, cover, path, lmod);
            }
            conn.commit();
            logger.info("Importing new tracks complete.");

            selectChangedTracksStmt.setString(1, device);
            rs = selectChangedTracksStmt.executeQuery();
            logger.info("Importing changes to existing tracks...");
            while (rs.next()) {
                long id = rs.getLong(1);
                String title = rs.getString(2);
                String artist = rs.getString(3);
                String album = rs.getString(4);
                String genre = rs.getString(5);
                int track = rs.getInt(6);
                int year =  rs.getInt(7);
                String cover = rs.getString(8);
                String path = rs.getString(9);
                Date lmod = rs.getDate(10);

                db.updateTrack(id, title, artist, album, genre, track, year, cover, path, lmod);
            }
            conn.commit();
            logger.info("Importing changed tracks complete.");

            rs = selectNewCoversStmt.executeQuery();
            logger.info("Importing new covers...");
            while (rs.next()) {
                String md5 = rs.getString(1);
                Blob data = rs.getBlob(2);
                int length = rs.getInt(3);
                String mimetype = rs.getString(4);

                db.insertCover(md5, data, length, mimetype);
            }
            conn.commit();
            logger.info("Cover import complete.");

            logger.info("Removing temporary tables...");
            Statement dropStmt = conn.createStatement();
            dropStmt.executeUpdate(String.format(DROP_TRACKS_TABLE, importTableSuffix));
            dropStmt.executeUpdate(String.format(DROP_COVERS_TABLE, importTableSuffix));
            conn.commit();
            logger.info("Import complete.");

        } catch (SQLException e) {
            logger.error("Import job {} interrupted by SQLException: {}", importTableSuffix);
            logger.error("Trace: ", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    // rollback failed O_o
                    logger.error("Unable to rollback changes of interrupted import job!!");
                }
            }
        } finally {
            JobManager.getInstance().removeJob(jobId);
        }
    }
}
