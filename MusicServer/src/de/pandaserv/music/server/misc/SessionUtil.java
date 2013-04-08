package de.pandaserv.music.server.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/17/13
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SessionUtil {

    static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);

    public static void setUserId(HttpSession session, long id) {
        //logger.info("set user id {} on session {}", id, request.getSession());
        session.setAttribute("userId", id);
    }

    /**
     * Get the active user id for the given session.
     * @param session the session to check
     * @return the active user's id or -1 when no user is logged in
     */
    public static long getUserId(HttpSession session) {
        Long ret = (Long) session.getAttribute("userId");
        if (ret == null) {
            //logger.info("session {} has user id {}", request.getSession(), ret);
            return -1;
        } else {
            return ret;
        }
    }

    public static void setLastTrackQueryId(HttpSession session, long id) {
        session.setAttribute("lastTrackQueryId", id);
    }

    public static long getLastTrackQueryId(HttpSession session) {
        Long ret = (Long) session.getAttribute("lastTrackQueryId");
        if (ret == null) {
            //logger.info("session {} has user id {}", request.getSession(), ret);
            return -1;
        } else {
            return ret;
        }
    }
}
