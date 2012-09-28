package de.pandaserv.music.server;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger.getLogger(Main.class.getName()).log(Level.INFO,
                "Starting Music Test Server...");

        // load startup configuration
        Properties startupConfig = new Properties();
        InputStream configIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("startup.cfg");
        if (configIn == null) {
            System.err.println("FATAL: unable to locate startup configuration file 'startup.cfg'");
            System.exit(1);
        }
        startupConfig.load(configIn);
        configIn.close();
        
        final MusicServer server = new MusicServer(startupConfig);
        
        server.start();
    }
}
