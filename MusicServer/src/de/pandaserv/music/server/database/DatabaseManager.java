package de.pandaserv.music.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    // connection properties
    // Class name of jdbc driver
    private static String DRIVER;
    // host running the database server
    private final String HOST;
    private final int PORT;
    // MYSQL username and password
    private final String USER;
    private final String PASSWORD;
    // name of the database to open
    private final String DATABASE;
    // jdbc url
    private final String JDBC_URL;
    // store a database connection for every thread
    private final ThreadLocal<Connection> threadLocalConnection;
    // Singleton
    private static DatabaseManager ourInstance;

    public static DatabaseManager getInstance() {
        return ourInstance;
    }

    public static DatabaseManager setup(Properties config) {
        if (ourInstance != null) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.WARNING,
                    "DatabaseManager.setup() called but there is already an instance!");
        } else {
            ourInstance = new DatabaseManager(config);
        }

        return ourInstance;
    }

    private DatabaseManager(Properties config) {
        //set the database properties
        DRIVER = config.getProperty("db_driver");
        HOST = config.getProperty("db_host");
        PORT = Integer.parseInt(config.getProperty("db_port"));
        USER = config.getProperty("db_user");
        PASSWORD = config.getProperty("db_password");
        DATABASE = config.getProperty("database");

        JDBC_URL = "jdbc:mysql://"
                + HOST + ":" + PORT + "/"
                + DATABASE + "";

        threadLocalConnection = new ThreadLocal<>();
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
                Logger.getLogger(DatabaseManager.class.getName()).log(Level.INFO,
                        "Created new database connection.");
                threadLocalConnection.set(newConnection);
                return newConnection;
            } else {
                //gets the existing connection
                return threadLocalConnection.get();
            }

        } catch (ClassNotFoundException dbDrvNotFound) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE,
                    "Database driver not found!");
            dbDrvNotFound.printStackTrace();
            System.exit(1);
        } catch (SQLException sqlException) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE,
                    "There was an error while creating a database connection.");
            sqlException.printStackTrace();
        }
        return null;
    }

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        // check database driver
        Class.forName(DRIVER);

        // make connection
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
