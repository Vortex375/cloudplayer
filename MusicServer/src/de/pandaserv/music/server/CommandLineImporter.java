package de.pandaserv.music.server;

import de.pandaserv.music.server.database.DatabaseManager;
import de.pandaserv.music.server.database.ImportJob;
import de.pandaserv.music.server.jobs.Job;
import de.pandaserv.music.server.jobs.JobManager;
import de.pandaserv.music.server.database.SqliteImportJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

/**
 * Helper class for importing Sqlite database from command line
 */
public class CommandLineImporter {
    private static class ProgressOutput extends Thread {
        private boolean run;
        private final JobManager jobManager;


        private ProgressOutput() {
            run = true;
            jobManager = JobManager.getInstance();
        }

        @Override
        public void run() {
            while(run) {
                Map<Long, Job> jobs = jobManager.listJobs();
                // TODO: this effectively only shows one job, but oh well...
                for (long key: jobs.keySet()) {
                    Job job = jobs.get(key);
                    System.out.print("\33[2K\r");
                    System.out.print(key + ": ");
                    System.out.print(job.getDescription() + ": ");
                    System.out.print(job.getStatus());
                    System.out.flush();
                }
                try {
                    sleep(33);
                } catch (InterruptedException e) {
                    // ?
                }
            }
        }

        public void finish() {
            run = false;
            try {
                join();
            } catch (InterruptedException e) {
                // ?
            }
        }
    }

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

        // get command line arguments
        String device = args[0];
        String dbPath = args[1];
        String tableSuffix = device + "_" + System.currentTimeMillis();

        // prepare the database
        logger.info("Preparing database for bulk insert...");
        Connection conn = DatabaseManager.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("SET FILES LOG FALSE");
        stmt.executeUpdate("CHECKPOINT");

        ProgressOutput progressOutput = new ProgressOutput();
        SqliteImportJob sqliteImport = new SqliteImportJob(dbPath, tableSuffix);
        logger.info("Running Sqlite import...");
        progressOutput.start();
        sqliteImport.run();
        progressOutput.finish(); // import job has no interesting progress output :-(
        logger.info("Sqlite import finished.");

        ImportJob importJob = new ImportJob(device, tableSuffix);
        logger.info("Running import job...");
        importJob.run();

        logger.info("Performing cleanup operations...");
        stmt.executeUpdate("SET FILES LOG TRUE");
        stmt.executeUpdate("CHECKPOINT DEFRAG");

        logger.info("Import finished.");
    }
}
