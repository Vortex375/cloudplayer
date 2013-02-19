package de.pandaserv.music.client.misc;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/19/13
 * Time: 2:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class JSUtil {
    public static native void log(String msg) /*-{
        console.log(msg);
    }-*/;
}
