package de.pandaserv.music.server.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/17/13
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SessionUtil {

    static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);

    public static void setUserId(HttpServletRequest request, long id) {
        //logger.info("set user id {} on session {}", id, request.getSession());
        request.getSession().setAttribute("userId", id);
    }

    /**
     * Get the active user id for the given request.
     * @param request the request to check
     * @return the active user's id or -1 when no user is logged in
     */
    public static long getUserId(HttpServletRequest request) {
        Long ret = (Long) request.getSession().getAttribute("userId");
        if (ret == null) {
            //logger.info("session {} has user id {}", request.getSession(), ret);
            return -1;
        } else {
            return ret;
        }
    }
}
