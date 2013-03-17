package de.pandaserv.music.server.misc;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/17/13
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SessionUtil {
    public static void setUserId(HttpServletRequest request, long id) {
        request.getSession().setAttribute("userId", id);
    }

    public static long getUserId(HttpServletRequest request) {
        Long ret = (Long) request.getSession().getAttribute("userId");
        if (ret == null) {
            return -1;
        } else {
            return ret;
        }
    }
}
