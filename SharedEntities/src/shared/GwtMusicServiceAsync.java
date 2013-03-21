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
    void prepare(long id, AsyncCallback<Void> async);

    // query methods
    void trackQuerySimple(String query, AsyncCallback<TrackDetail[]> async);

    void getTrackInfo(long id, AsyncCallback<Track> async);

    void getStatus(long id, AsyncCallback<FileStatus> async);

    void login(String username, String password, AsyncCallback<Long> async);

    void logout(AsyncCallback<Void> async);
}
