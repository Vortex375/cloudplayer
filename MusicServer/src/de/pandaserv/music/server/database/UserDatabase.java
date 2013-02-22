package de.pandaserv.music.server.database;

import de.pandaserv.music.server.misc.PasswordTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDatabase {

    static final Logger logger = LoggerFactory.getLogger(UserDatabase.class);

    private final LocalPreparedStatement addUser;

    // Singleton
    private static final UserDatabase ourInstance = new UserDatabase();

    public static UserDatabase getInstance() {
        return ourInstance;
    }

    private UserDatabase() {
        addUser = new LocalPreparedStatement("" +
                "INSERT INTO Users" +
                " (username, password, isAdmin)" +
                " VALUES" +
                " (?, ?, ?)");
    }

    public boolean addUser(String username, String password, boolean isAdmin) {
        PreparedStatement ps;
        ResultSet rs;

        // encode password
        String passwordEnc = PasswordTools.encodePassword(password);
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
    }
}
