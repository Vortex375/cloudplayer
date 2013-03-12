package de.pandaserv.music.server.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.shared.FileStatus;
import de.pandaserv.music.shared.GwtMusicService;
import de.pandaserv.music.shared.Track;
import de.pandaserv.music.shared.TrackDetail;
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
    public void testLogin() {
        long testId = System.currentTimeMillis();
        getThreadLocalRequest().getSession().setAttribute("test-userid", testId);
        logger.info("testLogin(): set random test user id={}", testId);
    }

    @Override
    public void prepare(long id) {
        CacheManager.getInstance().prepare(id);
    }

    @Override
    public TrackDetail[] trackQuerySimple(String query) {
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
    public Track getTrackInfo(long id) {
        return TrackDatabase.getInstance().getTrackInfo(id);
    }

    @Override
    public FileStatus getStatus(long id) {
        return CacheManager.getInstance().getStatus(id);
    }

}
