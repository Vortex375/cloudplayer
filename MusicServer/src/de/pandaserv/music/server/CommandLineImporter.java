package de.pandaserv.music.server;

import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.jobs.ImportJob;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.jobs.SqliteImportJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class for importing Sqlite database from command line
 */
public class CommandLineImporter {
    static final Logger logger = LoggerFactory.getLogger(CommandLineImporter.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: <device name> <database file>");
            System.exit(0);
        }

        // load JDBC drivers...
        //TODO: move this somewhere else
        Class.forName("org.sqlite.JDBC");
        Class.forName("org.hsqldb.jdbc.JDBCDriver");

        // load startup configuration
        Properties startupConfig = new Properties();
        InputStream configIn;
        // try to find config in working directory
        File configFile = new File("startup.cfg");
        if (configFile.exists()) {
            configIn = new FileInputStream(configFile);
        } else {
            // try to find config on classpath
            configIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("startup.cfg");
        }
        if (configIn == null) {
            logger.error("FATAL: unable to locate startup configuration file 'startup.cfg'");
            System.exit(1);
        }
        startupConfig.load(configIn);
        configIn.close();

        // initalize some basic application infrastructure
        JobManager.setup();
        DatabaseManager.setup(startupConfig);

        String device = args[0];
        String dbPath = args[1];
        String tableSuffix = device + "_" + System.currentTimeMillis();

        SqliteImportJob sqliteImport = new SqliteImportJob(dbPath, tableSuffix);
        logger.info("Running Sqlite import...");
        sqliteImport.run();
        logger.info("Sqlite import finished.");
        ImportJob importJob = new ImportJob(device, tableSuffix);
        logger.info("Running import job...");
        importJob.run();
        logger.info("Import finished.");
    }
}
