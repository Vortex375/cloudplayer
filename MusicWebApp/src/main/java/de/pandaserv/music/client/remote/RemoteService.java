package de.pandaserv.music.client.remote;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.view.client.Range;
import de.pandaserv.music.shared.*;

/**
 * Encapsulate the real remote service object for caching etc.
 */
public class RemoteService implements GwtMusicServiceAsync {

    private final GwtMusicServiceAsync service;

    private static RemoteService INSTANCE = null;

    private RemoteService() {
        // set up remote service
        service = GWT.create(GwtMusicService.class);
        ((ServiceDefTarget) service).setServiceEntryPoint("/service/gwt");
    }

    //SINGLETON
    public static RemoteService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteService();
        }
        return INSTANCE;
    }

    @Override
    public void trackQuerySimple(String query, AsyncCallback<RangeResponse<TrackDetail>> callback) {
        service.trackQuerySimple(query, callback);
    }

    @Override
    public void getTrackInfo(long id, AsyncCallback<Track> callback) {
        service.getTrackInfo(id, callback);
    }

    @Override
    public void login(String username, String password, AsyncCallback<UserInfo> callback) {
        service.login(username, password, callback);
    }

    @Override
    public void logout(AsyncCallback<Void> callback) {
        service.logout(callback);
    }

    @Override
    public void getCurrentUserInfo(AsyncCallback<UserInfo> callback) {
        service.getCurrentUserInfo(callback);
    }

    @Override
    public void getTrackDetailRange(long queryId, Range range, AsyncCallback<RangeResponse<TrackDetail>> callback) {
        service.getTrackDetailRange(queryId, range, callback);
    }
}
