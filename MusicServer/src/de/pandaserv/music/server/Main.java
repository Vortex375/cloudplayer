package de.pandaserv.music.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Music Test Server...");

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

        final MusicServer server = new MusicServer(startupConfig);

        server.start();
    }
}
