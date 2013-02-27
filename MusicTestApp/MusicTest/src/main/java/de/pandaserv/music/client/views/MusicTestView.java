package de.pandaserv.music.client.views;

import com.google.gwt.dom.client.AudioElement;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.shared.Track;
import de.pandaserv.music.shared.TrackDetail;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MusicTestView {
    public interface Presenter {
        void playToggle();

        void newSearchQuery();
        void onSearchResultClicked(int index);

        void seekTo(double seconds);
    }

    void showError(boolean show);
    void setErrorMessage(String message);

    void setSearchResults(TrackDetail[] results);
    void showWaitOnResult(int index, boolean show);
    void setCurrentTrackInfo(Track track);

    void setDuration(double seconds);
    void setTime(double seconds);
    void setPlaybackStatus(PlaybackStatus status);
    void setVisData(int[] bars);

    void setDebugString(String debug);

    String getSearchQuery();

    void setPresenter(Presenter presenter);
}
