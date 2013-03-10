package de.pandaserv.music.server.database;

import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
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

    private static final String MERGE_COVERS = "" +
            "MERGE INTO Covers AS c" +
            " USING import_covers_%s AS i" +
            " ON c.md5=i.md5" +
            " WHEN NOT MATCHED THEN INSERT (md5, data, length, mimetype)" +
            " VALUES (i.md5, i.data, i.length, i.mimetype)";

    private static final String MERGE_TRACKS = "" +
            "MERGE INTO Tracks AS t" +
            // here, we add the bogus column "device" to the import table
            // because using a string constant in the ON clause apparently does not work
            " USING (SELECT ? AS device, id, title, artist, album, genre, track, year, cover, path, lastmodified FROM import_tracks_%s) AS i" +
            " ON t.device=i.device AND t.device_id=i.id" +
            " WHEN MATCHED THEN UPDATE SET" +
            "       t.title = i.title, t.artist = i.artist, t.album = i.album, t.genre = i.genre," +
            "       t.track = i.track, t.year = i.year, t.cover = i.cover, " +
            "       t.device_path = i.path, t.lastmodified = i.lastmodified" +
            " WHEN NOT MATCHED THEN INSERT (device, device_id, title, artist, album, genre," +
            "       track, year, cover, device_path, lastmodified)" +
            " VALUES (i.device, i.id, i.title, i.artist, i.album, i.genre, i.track, i.year, i.cover, i.path, i.lastmodified)";

    private static final String REMOVE_DELETED_TRACKS = "" +
            "DELETE FROM Tracks " +
            " WHERE device=?" +
            "  AND device_id NOT IN" +
            "       (SELECT id FROM import_tracks_%s)";

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
            //conn.setAutoCommit(false);
            PreparedStatement mergeTracksStmt = conn.prepareStatement(String.format(MERGE_TRACKS, importTableSuffix));
            mergeTracksStmt.setString(1, device);
            PreparedStatement deleteTracksStmt = conn.prepareStatement(String.format(REMOVE_DELETED_TRACKS, importTableSuffix));
            deleteTracksStmt.setString(1, device);
            //mergeTracksStmt.setString(2, device);
            PreparedStatement mergeCoversStmt = conn.prepareStatement(String.format(MERGE_COVERS, importTableSuffix));

            logger.info("Merging tracks...");
            int affectedRows = mergeTracksStmt.executeUpdate();
            logger.info("Warnings during merge: {}", mergeTracksStmt.getWarnings());
            logger.info("{} tracks merged.", affectedRows);

            logger.info("Deleting old entries...");
            affectedRows = deleteTracksStmt.executeUpdate();
            logger.info("Warnings during deletion: {}", deleteTracksStmt.getWarnings());
            logger.info("{} tracks deleted.", affectedRows);

            logger.info("Merging covers...");
            affectedRows = mergeCoversStmt.executeUpdate();
            logger.info("{} covers imported.", affectedRows);

            logger.info("Removing temporary tables...");
            Statement dropStmt = conn.createStatement();
            dropStmt.executeUpdate(String.format(DROP_TRACKS_TABLE, importTableSuffix));
            dropStmt.executeUpdate(String.format(DROP_COVERS_TABLE, importTableSuffix));
            //logger.info("Committing changes...");
            //conn.commit();
            logger.info("Import complete.");

        } catch (SQLException e) {
            logger.error("Import job {} interrupted by SQLException: {}", importTableSuffix);
            logger.error("Trace: ", e);
            /*try {
                conn.rollback();
            } catch (SQLException e1) {
                // rollback failed O_o
                logger.error("Unable to rollback changes of interrupted import job!!");
            }*/
        } finally {
            JobManager.getInstance().removeJob(jobId);
        }
    }
}
