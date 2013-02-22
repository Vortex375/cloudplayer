package de.pandaserv.music.server.jobs;

import de.pandaserv.music.server.database.DatabaseManager;
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
public class SqliteImportJob implements Job {
    static final Logger logger = LoggerFactory.getLogger(SqliteImportJob.class);

    private int totalRows;
    private int position;
    private final String dbPath;
    private final String importTableSuffix;
    private State state;
    private boolean cancelled;

    private static final String CREATE_TRACKS_TABLE = "" +
            "CREATE CACHED TABLE import_tracks_%s (" +
            " id BIGINT PRIMARY KEY," +
            " title VARCHAR(200)," +
            " artist VARCHAR(200)," +
            " album VARCHAR(200)," +
            " genre VARCHAR(200)," +
            " track INTEGER," +
            " year INTEGER," +
            " cover VARCHAR(200)," +
            " path VARCHAR(800)," +
            " lastmodified TIMESTAMP)";
    private static final String CREATE_COVERS_TABLE =
            "CREATE CACHED TABLE import_covers_%s (" +
            " md5 VARCHAR(200) PRIMARY KEY," +
            " data BLOB," +
            " length INTEGER," +
            " mimetype VARCHAR(50))";
    private static final String INSERT_TRACK =
            "INSERT INTO import_tracks_%s" +
            " (id, title, artist, album, genre, track, year, cover, path, lastmodified)" +
            " VALUES" +
            " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_COVER =
            "INSERT INTO import_covers_%s" +
            " (md5, data, length, mimetype)" +
            " VALUES" +
            " (?, ?, ?, ?)";

    private static enum State {
        INITIAL,
        TRACKS,
        COVERS
    }

    public SqliteImportJob(String dbPath, String importTableSuffix) {
        this.dbPath = dbPath;
        this.importTableSuffix = importTableSuffix;
        cancelled = false;
    }

    @Override
    public String getDescription() {
        return "Sqlite import job " + importTableSuffix;
    }

    @Override
    public synchronized String getStatus() {
        switch (state) {
            case INITIAL:
                return "Preparing...";
            case TRACKS:
                return String.format("Copying tracks %d%% (%d/%d)", (position / totalRows) * 100, position, totalRows);
            case COVERS:
                return String.format("Copying covers %d%% (%d/%d)", (position / totalRows) * 100, position, totalRows);
        }
        return "...";
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
            ResultSet rs;
            logger.info("Open sqlite database file...");
            Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Statement sqliteStmt= sqliteConn.createStatement();

            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            logger.info("Creating import tables...");
            stmt.executeUpdate(String.format(CREATE_TRACKS_TABLE, importTableSuffix));
            stmt.executeUpdate(String.format(CREATE_COVERS_TABLE, importTableSuffix));

            // prepare statements after tables have been created
            PreparedStatement trackStmt = conn.prepareStatement(String.format(INSERT_TRACK, importTableSuffix));
            PreparedStatement coverStmt = conn.prepareStatement(String.format(INSERT_COVER, importTableSuffix));

            rs = sqliteStmt.executeQuery("SELECT COUNT(*) FROM tracks;");
            rs.next();
            synchronized (this) {
                totalRows = rs.getInt(1);
                position = 0;
                state = State.TRACKS;
            }
            rs = sqliteStmt.executeQuery("SELECT id, title, artist, album, genre, track, year, cover, path, lastmodified" +
                    " FROM tracks;");
            logger.info("Copying {} tracks...", totalRows);
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

                trackStmt.setLong(1, id);
                trackStmt.setString(2, title);
                trackStmt.setString(3, artist);
                trackStmt.setString(4, album);
                trackStmt.setString(5, genre);
                trackStmt.setInt(6, track);
                trackStmt.setInt(7, year);
                trackStmt.setString(8, cover);
                trackStmt.setString(9, path);
                trackStmt.setDate(10, lmod);
                trackStmt.executeUpdate();

                synchronized (this) {
                    position++;
                }
            }
            logger.info("Commit changes...");
            conn.commit();
            logger.info("Copy tracks complete.");

            rs = sqliteStmt.executeQuery("SELECT COUNT(*) FROM covers;");
            rs.next();
            synchronized (this) {
                totalRows = rs.getInt(1);
                position = 0;
                state = State.COVERS;
            }

            rs = sqliteStmt.executeQuery("SELECT md5, data, length, mimetype FROM covers;");
            logger.info("Copying {} covers...", totalRows);
            while (rs.next()) {
                String md5 = rs.getString(1);
                Blob data = rs.getBlob(2);
                int length = rs.getInt(3);
                String mimetype = rs.getString(4);

                coverStmt.setString(1, md5);
                coverStmt.setBlob(2, data);
                coverStmt.setInt(3, length);
                coverStmt.setString(4, mimetype);
                coverStmt.executeUpdate();

                synchronized (this) {
                    position++;
                }
            }

            logger.info("Commit changes...");
            conn.commit();
            logger.info("Sqlite import complete");

        } catch (SQLException e) {
            logger.error("Sqlite import job {} interrupted by SQLException: {}", importTableSuffix);
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
