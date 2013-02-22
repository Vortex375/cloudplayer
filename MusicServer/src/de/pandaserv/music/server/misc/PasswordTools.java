package de.pandaserv.music.server.misc;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * PasswordTools
 *
 * Tools for dealing with passwords.
 *
 */
public class PasswordTools {
    /**
     * Encode the given plain-text password using SHA-1
     * with the current timestamp as salt.
     * @param password
     * @return the encrypted password
     */
    public static String encodePassword(String password) {
        try {
            String salt = SHAsum(("" + System.currentTimeMillis()).getBytes());
            //TODO: really use UTF-8 charset for passwords?
            String enc = SHAsum((password + salt).getBytes(Charset.forName("UTF-8")));
            return enc + salt;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Check if the given password matches the given password hash.
     *
     * The password hash must be in the format as returned by encodePassword().
     *
     * @param password a plain-text password
     * @param pwHash an encoded password-hash
     * @return true if the password matches the hash, otherwise false
     */
    public static boolean checkPassword(String password, String pwHash) {
        if (pwHash.length() != 80) {
            // invalid hash
            return false;
        }
        try {
            String salt = pwHash.substring(40, pwHash.length());
            String enc = SHAsum((password + salt).getBytes(Charset.forName("UTF-8")));
            return (enc + salt).equals(pwHash);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
