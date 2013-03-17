package de.pandaserv.music.server.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
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


    @Override
    public long login(String username, String password) {
        if (SessionUtil.getUserId(getThreadLocalRequest()) >= 0) {
            // there is already a user logged in
            logout();
        }
        long ret = UserDatabase.getInstance().checkLogin(username, password);
        if (ret > 0) {
            SessionUtil.setUserId(getThreadLocalRequest(), ret);
        }
        return ret;
    }

    @Override
    public void logout() {
        getThreadLocalRequest().getSession().invalidate();
    }

    @Override
    public void prepare(long id) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        CacheManager.getInstance().prepare(id);
    }

    @Override
    public TrackDetail[] trackQuerySimple(String query) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        query = query.trim();
        if (query.length() < 3) {
            // reject very short queries
            return new TrackDetail[0];
        }
        List<TrackDetail> list = TrackDatabase.getInstance().trackQuerySimple(query);
        if (list == null) {
            // exception in query - this shouldn't normally happen
            // return empty result
            return new TrackDetail[0];
        }

        return list.toArray(new TrackDetail[list.size()]);
    }

    @Override
    public Track getTrackInfo(long id) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        return TrackDatabase.getInstance().getTrackInfo(id);
    }

    @Override
    public FileStatus getStatus(long id) throws AccessDeniedException {
        if (SessionUtil.getUserId(getThreadLocalRequest()) < 0) {
            // not logged in
            throw new AccessDeniedException();
        }
        return CacheManager.getInstance().getStatus(id);
    }

}
