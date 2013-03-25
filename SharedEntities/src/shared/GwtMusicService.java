package de.pandaserv.music.shared;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/21/13
 * Time: 3:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GwtMusicService extends RemoteService {
    public long login(String username, String password);
    public void logout();
    public long getCurrentUserId();

    public void prepare(long id) throws AccessDeniedException;

    // query methods
    public TrackDetail[] trackQuerySimple(String query) throws AccessDeniedException;
    public Track getTrackInfo(long id) throws AccessDeniedException;

    public FileStatus getStatus(long id) throws AccessDeniedException;
}
