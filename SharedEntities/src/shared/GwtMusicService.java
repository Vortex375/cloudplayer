package de.pandaserv.music.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.core.client.GWT;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/21/13
 * Time: 3:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GwtMusicService extends RemoteService {
    public String test();

    public void prepare(long id);

    // query methods
    public TrackDetail[] trackQuerySimple(String query);

    public Track getTrackInfo(long id);
    public FileStatus getStatus(long id);
}
