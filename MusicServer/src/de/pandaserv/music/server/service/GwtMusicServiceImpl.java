package de.pandaserv.music.server.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.shared.GwtMusicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public String test() {
        logger.info("Hello World!");
        return "Hello World!";
    }

    @Override
    public void prepare(long id) {
        CacheManager.getInstance().prepare(id);
    }
}
