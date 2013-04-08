package de.pandaserv.music.server.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;
import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.database.UserDatabase;
import de.pandaserv.music.server.misc.SessionUtil;
import de.pandaserv.music.shared.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/21/13
 * Time: 3:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class GwtMusicServiceImpl extends RemoteServiceServlet implements GwtMusicService {

    static final Logger logger = LoggerFactory.getLogger(GwtMusicServiceImpl.class);

    private static final int INITIAL_RANGE_SIZE = 30; // how many rows to initially return for range request

    @Override
    public UserInfo login(String username, String password) {
        if (SessionUtil.getUserId(getThreadLocalRequest().getSession()) >= 0) {
            /*
             * there is already a user logged in in this session
             * log out the old user before proceeding
             */
            logout();
        }
        /*
         * check username and password
         */
        long id = UserDatabase.getInstance().checkLogin(username, password);
        if (id < 0) {
            /*
             * incorrect username or password
             */
            return null;
        } else {
            /*
             * login successful
             *
             * store user id in current session and return a UserInfo object
             */
            SessionUtil.setUserId(getThreadLocalRequest().getSession(), id);
            return new UserInfo(id, username);
        }
    }

    @Override
    public void logout() {
        getThreadLocalRequest().getSession().invalidate();
    }

    @Override
    public UserInfo getCurrentUserInfo() {
        long id = SessionUtil.getUserId(getThreadLocalRequest().getSession());
        if (id < 0) {
            /*
             * no user logged in
             */
            return null;
        } else {
            String username = UserDatabase.getInstance().getUsername(id);
            if (username == null) {
                /*
                 * current user is not in database !?
                 */
                logger.warn("getCurrentUserInfo(): the active user ({}) was not found in the database!", id);
                return null;
            } else {
                return new UserInfo(id, username);
            }
        }
    }

    @Override
    public void prepare(long id) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest().getSession()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        CacheManager.getInstance().prepare(id);
    }

    @Override
    public RangeResponse<TrackDetail> trackQuerySimple(String query) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest().getSession()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        query = query.trim();

        List<TrackDetail> resultList = TrackDatabase.getInstance().trackQuerySimple(query);
        if (resultList == null) {
            // exception in query - this shouldn't normally happen
            // return empty result
            return new RangeResponse<TrackDetail>(0, new TrackDetail[0], new Range(0, 0), 0);
        }

        // clear previous query
        long queryId = SessionUtil.getLastTrackQueryId(getThreadLocalRequest().getSession());
        RequestCache.getInstance().drop(queryId);

        // store new query
        TrackDetail[] resultArray = resultList.toArray(new TrackDetail[resultList.size()]);
        queryId = System.currentTimeMillis() | Thread.currentThread().getId(); // new query id
        RequestCache.getInstance().put(queryId, resultArray);
        SessionUtil.setLastTrackQueryId(getThreadLocalRequest().getSession(), queryId);

        return getTrackDetailRange(queryId, new Range(0, INITIAL_RANGE_SIZE));
    }

    @Override
    public Track getTrackInfo(long id) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest().getSession()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        return TrackDatabase.getInstance().getTrackInfo(id);
    }

    @Override
    public FileStatus getStatus(long id) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest().getSession()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        return CacheManager.getInstance().getStatus(id);
    }

    @Override
    public  RangeResponse<TrackDetail> getTrackDetailRange(long queryId, Range range) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest().getSession()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }

        TrackDetail[] data;
        try {
            data = (TrackDetail[]) RequestCache.getInstance().get(queryId);
        } catch (ClassCastException e) {
            // wrong data type stored here
            data = null;
        }

        if (data == null) {
            return null;
        }

        TrackDetail[] ret = new TrackDetail[range.getLength()];
        System.arraycopy(data, range.getStart(), ret, 0, range.getLength());

        return new RangeResponse<>(queryId, ret, range, data.length);
    }

}
