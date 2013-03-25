package de.pandaserv.music.client.remote;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
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
    public void prepare(long id, AsyncCallback<Void> callback) {
        service.prepare(id, callback);
    }

    @Override
    public void trackQuerySimple(String query, AsyncCallback<TrackDetail[]> callback) {
        service.trackQuerySimple(query, callback);
    }

    @Override
    public void getTrackInfo(long id, AsyncCallback<Track> callback) {
        service.getTrackInfo(id, callback);
    }

    @Override
    public void getStatus(long id, AsyncCallback<FileStatus> callback) {
        service.getStatus(id, callback);
    }

    @Override
    public void login(String username, String password, AsyncCallback<Long> callback) {
        service.login(username, password, callback);
    }

    @Override
    public void logout(AsyncCallback<Void> callback) {
        service.logout(callback);
    }

    @Override
    public void getCurrentUserId(AsyncCallback<Long> callback) {
        service.getCurrentUserId(callback);
    }
}
