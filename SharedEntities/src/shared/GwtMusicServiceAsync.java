package de.pandaserv.music.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/21/13
 * Time: 3:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GwtMusicServiceAsync {
    void prepare(long id, AsyncCallback<Void> callback);

    // query methods
    void trackQuerySimple(String query, AsyncCallback<TrackDetail[]> callback);

    void getTrackInfo(long id, AsyncCallback<Track> callback);

    void getStatus(long id, AsyncCallback<FileStatus> callback);

    void login(String username, String password, AsyncCallback<UserInfo> async);

    void logout(AsyncCallback<Void> callback);

    void getCurrentUserInfo(AsyncCallback<UserInfo> callback);
}
