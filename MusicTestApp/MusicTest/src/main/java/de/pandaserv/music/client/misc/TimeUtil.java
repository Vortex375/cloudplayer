package de.pandaserv.music.client.misc;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/12/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeUtil {
    public static String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        seconds = seconds - (hours * 3600);
        int minutes = (int) (seconds / 60);
        seconds = seconds - (minutes * 60);

        String secString = (seconds < 10 ? "0" + (int) seconds : "" +  (int) seconds);
        String ret;
        if (hours > 0) {
            ret = hours + ":" + minutes + ":" + secString;
        } else {
            ret = minutes + ":" + secString;
        }
        return ret;
    }
}
