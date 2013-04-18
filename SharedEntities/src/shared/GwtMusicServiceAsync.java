package de.pandaserv.music.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/21/13
 * Time: 3:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GwtMusicServiceAsync {
    // query methods
    void trackQuerySimple(String query, AsyncCallback<RangeResponse<TrackDetail>> async);

    void getTrackInfo(long id, AsyncCallback<Track> callback);

    void login(String username, String password, AsyncCallback<UserInfo> async);

    void logout(AsyncCallback<Void> callback);

    void getCurrentUserInfo(AsyncCallback<UserInfo> callback);

    void getTrackDetailRange(long queryId, Range range, AsyncCallback<RangeResponse<TrackDetail>> async);

    // vfs methods
    void listDir(String device, String path, AsyncCallback<RangeResponse<Directory>> async);

    void listDir(long id, AsyncCallback<RangeResponse<Directory>> async);

    void updateVfs(AsyncCallback<Boolean> async);

    void rebuildVfs(AsyncCallback<Boolean> async);
}
