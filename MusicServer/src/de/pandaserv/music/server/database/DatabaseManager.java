package de.pandaserv.music.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private static final String CREATE_TRACKS_TABLE =
            "CREATE CACHED TABLE Tracks (" +
            " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
            " device VARCHAR(200)," +
            " device_id BIGINT," +
            " title VARCHAR(200)," +
            " artist VARCHAR(200)," +
            " album VARCHAR(200)," +
            " genre VARCHAR(200)," +
            " track INTEGER," +
            " year INTEGER," +
            " duration INTEGER," +
            " fileSize BIGINT," +
            " cover VARCHAR(200)," +
            " device_path VARCHAR(800)," +
            " lastmodified TIMESTAMP," +
            " UNIQUE (device, device_id))";

    private static final String CREATE_COVERS_TABLE =
            "CREATE CACHED TABLE Covers (" +
            " md5 VARCHAR(200) PRIMARY KEY," +
            " data LONGVARBINARY," +
            " length INTEGER," +
            " mimetype VARCHAR(50))";

    private static final String CREATE_CACHE_TABLE =
            "CREATE CACHED TABLE CacheIndex (" +
            " id BIGINT PRIMARY KEY," +
            " available BIGINT)";

    private static final String CREATE_VIRTUALFS_TABLE =
            "CREATE CACHED TABLE VirtualFS (" +
            " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
            " parent BIGINT NULL," +
            " device VARCHAR(200)," +
            " path VARCHAR(800)," +
            " trackId BIGINT NULL," +
            " UNIQUE (device, path)," +
            " FOREIGN KEY (parent) REFERENCES VirtualFS(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            " FOREIGN KEY (trackId) REFERENCES Tracks(id) ON UPDATE CASCADE ON DELETE CASCADE)";

    private static final String CREATE_DEVICES_TABLE =
            "CREATE CACHED TABLE Devices (" +
            " name VARCHAR(200) PRIMARY KEY," +
            " type VARCHAR(200)," +
            " cache BOOLEAN)";

    private static final String CREATE_ATTRIBUTES_TABLE =
            "CREATE CACHED TABLE Attributes (" +
            " object VARCHAR(200)," +
            " key VARCHAR(200)," +
            " value LONGVARCHAR," +
            " private BOOLEAN," +
            " PRIMARY KEY (object, key))";

    private static final String CREATE_USERS_TABLE =
            "CREATE CACHED TABLE Users (" +
            " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
            " username VARCHAR(200) UNIQUE," +
            " password VARCHAR(200)," +
            " isAdmin BOOLEAN)";

    private static final String CREATE_PLAYLISTS_TABLE =
            "CREATE CACHED TABLE Playlists (" +
            " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
            " name VARCHAR(200)," +
            " owner BIGINT," +
            " FOREIGN KEY (owner) REFERENCES Users(id) ON UPDATE CASCADE ON DELETE CASCADE)";

    private static final String CREATE_PLAYLIST_TRACKS_TABLE =
            "CREATE CACHED TABLE Playlist_Tracks (" +
            " playlist BIGINT," +
            " pos INTEGER," +
            " track BIGINT," +
            " FOREIGN KEY (playlist) REFERENCES Playlists(id) ON UPDATE CASCADE ON DELETE CASCADE," +
            " FOREIGN KEY (track) REFERENCES Tracks(id) ON UPDATE CASCADE ON DELETE CASCADE)";

    // default admin user account
    // inserted automatically when the user table is created for the first time
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    // path to HSQLDB database file
    private final String DB_PATH;
    // username and password for HSQLDB (this is always "SA" with no password)
    private final String USER = "SA";
    private final String PASSWORD = "";
    // jdbc url
    private final String JDBC_URL;
    // store a database connection for every thread
    private final ThreadLocal<Connection> threadLocalConnection;
    // Singleton
    private static DatabaseManager ourInstance;

    public static DatabaseManager getInstance() {
        return ourInstance;
    }

    public static DatabaseManager setup(Properties config) throws SQLException {
        if (ourInstance != null) {
            logger.warn("DatabaseManager.setup() called but there is already an instance!");
        } else {
            ourInstance = new DatabaseManager(config);
            ourInstance.checkDatabase();
        }

        return ourInstance;
    }

    public void shutdown() {
        logger.info("Running shutdown hook: sending SHUTDOWN command to database.");
        try {
            getConnection().createStatement().executeUpdate("SHUTDOWN");
            logger.info("Database shut down.");
        } catch (SQLException e) {
            logger.error("Unable to close database. There should be no data loss" +
                    " but the next startup will take longer because HSQLDB will do a transaction replay.");
            logger.error("Trace: " + e);
        }
    }

    private DatabaseManager(Properties config) throws SQLException {
        //set the database properties
        DB_PATH = config.getProperty("db_dir");
        JDBC_URL = "jdbc:hsqldb:file:" + DB_PATH + "/db";
        threadLocalConnection = new ThreadLocal<>();
    }

    /* called on startup
     * check database and create missing tables
     */
    private void checkDatabase() throws SQLException {
        logger.info("Checking database...");
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Unable to connect to database.");
        }
        Statement stmt = conn.createStatement();
        ResultSet rs;

        // check if tables exist
        // TODO: also check columns
        boolean needTracksTable = false;
        boolean needCoversTable = false;
        boolean needCacheTable = false;
        boolean needVirtualFSTable = false;
        boolean needUsersTable = false;
        boolean needAttributesTable = false;
        boolean needDevicesTable = false;
        boolean needPlaylistsTable = false;
        boolean needPlaylistTracksTable = false;


        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='TRACKS'");
        needTracksTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='COVERS'");
        needCoversTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='CACHEINDEX'");
        needCacheTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='VIRTUALFS'");
        needVirtualFSTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='USERS'");
        needUsersTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='ATTRIBUTES'");
        needAttributesTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='DEVICES'");
        needDevicesTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='PLAYLISTS'");
        needPlaylistsTable = !rs.next();
        rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='PLAYLIST_TRACKS'");
        needPlaylistTracksTable = !rs.next();

        if (needTracksTable) {
            logger.info("Creating table 'Tracks'");
            stmt.executeUpdate(CREATE_TRACKS_TABLE);
        }
        if (needCoversTable) {
            logger.info("Creating table 'Covers'");
            stmt.executeUpdate(CREATE_COVERS_TABLE);
        }
        if (needCacheTable) {
            logger.info("Creating table 'CacheIndex'");
            stmt.executeUpdate(CREATE_CACHE_TABLE);
        }
        if (needVirtualFSTable) {
            logger.info("Creating table 'VirtualFS'");
            stmt.executeUpdate(CREATE_VIRTUALFS_TABLE);
        }
        if (needUsersTable) {
            logger.info("Creating table 'Users'");
            stmt.executeUpdate(CREATE_USERS_TABLE);
            // add default admin account
            logger.warn("Adding default administrator account (username=\"{}\", password=\"{}\")." +
                    " Remember to log in and change the password!!", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
            if (!UserDatabase.getInstance().addUser(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, true)) {
                throw new SQLException("unable to add default admin user account!");
            }
        }
        if (needAttributesTable) {
            logger.info("Creating table 'Attributes'");
            stmt.executeUpdate(CREATE_ATTRIBUTES_TABLE);
        }
        if (needDevicesTable) {
            logger.info("Creating table 'Devices'");
            stmt.executeUpdate(CREATE_DEVICES_TABLE);
            logger.info("Adding default device for local file access (name=\"local\", type=\"local\")");
            DeviceDatabase.getInstance().addDevice("local", "local", false);
        }
        if (needPlaylistsTable) {
                logger.info("Creating table 'Playlists'");
                stmt.executeUpdate(CREATE_PLAYLISTS_TABLE);
        }
        if (needPlaylistTracksTable) {
            logger.info("Creating table 'Playlist_Tracks'");
            stmt.executeUpdate(CREATE_PLAYLIST_TRACKS_TABLE);
        }
        logger.info("Database setup complete.");
    }

    /**
     * Closes the database connection for the current Thread. Will do nothing if
     * there is no open connection.
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        Connection connection = threadLocalConnection.get();

        if (connection != null) {
            connection.close();
            threadLocalConnection.remove();
        }
    }

    /**
     * Get the database connection for the current Thread. If there is no open
     * connection a new connection will be made.
     *
     * @return a database connection object
     */
    public Connection getConnection() {
        try {
            //checks if the connection was not created
            // OR if the connection has been closed or is not valid (e.g. due to a timeout)
            if (threadLocalConnection.get() == null || !threadLocalConnection.get().isValid(2)) {
                //creates a new connection
                Connection newConnection = createConnection();
                logger.info("Created new database connection.");
                threadLocalConnection.set(newConnection);
                return newConnection;
            } else {
                //gets the existing connection
                return threadLocalConnection.get();
            }

        } catch (ClassNotFoundException dbDrvNotFound) {
            logger.error("Database driver not found!");
            dbDrvNotFound.printStackTrace();
            System.exit(1);
        } catch (SQLException sqlException) {
            logger.error("There was an error while creating a database connection.");
            sqlException.printStackTrace();
        }
        return null;
    }

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        // check database driver
        //Class.forName(DRIVER);

        // make connection
        logger.info("Connecting to " + JDBC_URL);
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
