package de.pandaserv.music.server;

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
        InputStream configIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("startup.cfg");
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
