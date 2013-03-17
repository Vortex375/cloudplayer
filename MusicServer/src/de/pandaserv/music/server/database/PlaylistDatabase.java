package de.pandaserv.music.server.database;

public class PlaylistDatabase {
/*
    static final Logger logger = LoggerFactory.getLogger(PlaylistDatabase.class);

    private final LocalPreparedStatement createPlaylist;
    private final LocalPreparedStatement renamePlaylist;
    private final LocalPreparedStatement deletePlaylist;

    private final LocalPreparedStatement insertTrack;
    private final LocalPreparedStatement incrementPos;
    private final LocalPreparedStatement decrementPos;

    // Singleton
    private static final PlaylistDatabase ourInstance = new PlaylistDatabase();

    public static PlaylistDatabase getInstance() {
        return ourInstance;
    }

    private PlaylistDatabase() {
        createPlaylist = new LocalPreparedStatement("" +
                "INSERT INTO Users" +
                " (username, password, isAdmin)" +
                " VALUES" +
                " (?, ?, ?)");
    }

    public boolean addUser(String username, String password, boolean isAdmin) {
        PreparedStatement ps;
        ResultSet rs;

        // encode password
        String passwordEnc = PasswordUtil.encodePassword(password);
        if (passwordEnc == null) {
            logger.warn("not adding user \"{}\": unable to encode password!", username);
            return false;
        }

        ps = addUser.get();
        try {
            ps.setString(1, username);
            ps.setString(2, passwordEnc);
            ps.setBoolean(3, isAdmin);
            ps.executeUpdate();
            logger.info("Added new user \"{}\"", username);
            return true;
        } catch (SQLException e) {
            logger.warn("SQLException while adding user \"{}\". User already exists?", username);
            logger.warn(e.toString());
            return false;
        }
    }*/
}
