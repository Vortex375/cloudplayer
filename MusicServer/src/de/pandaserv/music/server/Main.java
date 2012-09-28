package de.pandaserv.music.server;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger.getLogger(Main.class.getName()).log(Level.INFO,
                "Starting Music Test Server...");

        MusicServer server = new MusicServer(8081);
        
        server.start();
    }
}
