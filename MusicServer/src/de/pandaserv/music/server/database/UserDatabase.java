package de.pandaserv.music.server.database;

import de.pandaserv.music.server.misc.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDatabase {

    static final Logger logger = LoggerFactory.getLogger(UserDatabase.class);

    private final LocalPreparedStatement addUser;
    private final LocalPreparedStatement getPassword;

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
        getPassword = new LocalPreparedStatement("" +
                "SELECT id, password" +
                " FROM Users" +
                " WHERE username=?");
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
    }

    public long checkLogin(String username, String password) {
        PreparedStatement ps;
        ResultSet rs;

        ps = getPassword.get();
        try {
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                long userId = rs.getLong(1);
                String passwordDb = rs.getString(2);
                if (PasswordUtil.checkPassword(password, passwordDb)) {
                    return userId;
                } else {
                    // wrong password
                    return -1;
                }
            } else {
                // user not found
                return -1;
            }
        } catch (SQLException e) {
            logger.warn("SQLException while checking login for user \"{}\"!", username);
            logger.warn("Trace: ", e);
            return -1;
        }
    }
}
