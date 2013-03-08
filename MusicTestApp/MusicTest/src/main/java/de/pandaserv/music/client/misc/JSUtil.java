package de.pandaserv.music.client.misc;

import com.google.gwt.user.client.Element;

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

    public static native void sleep(int ms) /*-{
        var start = new Date().getTime();
        for (var i = 0; i < 1e7; i++) {
            if ((new Date().getTime() - start) > ms){
                break;
            }
        }
    }-*/;

    public static native void addGlisse(Element anchor) /*-{
        $wnd.$(anchor).glisse({}); // use default options
    }-*/;
}
