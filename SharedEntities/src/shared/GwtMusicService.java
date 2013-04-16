package de.pandaserv.music.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.view.client.Range;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/21/13
 * Time: 3:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GwtMusicService extends RemoteService {
    /**
     * Log in with given username and password
     * @param username username
     * @param password password in plain text
     * @return return UserInfo containing username and id when login successful; null otherwise
     */
    public UserInfo login(String username, String password);
    public void logout();
    public UserInfo getCurrentUserInfo();

    // query methods
    public RangeResponse<TrackDetail> trackQuerySimple(String query) throws AccessDeniedException;
    public Track getTrackInfo(long id) throws AccessDeniedException;

    // vfs methods
    public RangeResponse<Directory> listDir(String device, String path) throws AccessDeniedException;
    public RangeResponse<Directory> listDir(long id) throws AccessDeniedException;
    public boolean updateVfs() throws AccessDeniedException;
    public boolean rebuildVfs() throws AccessDeniedException;

    public RangeResponse<TrackDetail> getTrackDetailRange(long queryId, Range range) throws AccessDeniedException;
}
