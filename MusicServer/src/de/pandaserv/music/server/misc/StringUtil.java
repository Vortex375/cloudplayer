package de.pandaserv.music.server.misc;

import java.text.ParseException;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/23/13
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtil {
    public static long parseSize(String s) {
        /*s = s.trim();
        if (!s.matches("[0-9]+[KMG]]")) {
            throw new RuntimeException("invalid size: " + s);
        }*/
        long size = Long.parseLong(s.substring(0, s.length() - 1));
        int mult;
        switch(s.charAt(s.length() - 1)) {
            case 'K':
                mult = 1024;
                break;
            case 'M':
                mult = 1024 * 1024;
                break;
            case 'G':
                mult = 1024 * 1024 * 1024;
                break;
            default:
                throw new RuntimeException("invalid size modifier: only 'K', 'M' and 'G' is allowed");
        }
        return size * mult;
    }
}
