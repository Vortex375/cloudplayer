package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.shared.Track;

public interface PlaybackView extends IsWidget {
    public interface Presenter {
        void playToggle();
        void seekTo(double seconds);
    }

    void showPlaybackWaiting(boolean show);
    void setCurrentTrackInfo(Track track);
    void setDuration(double seconds);
    void setTime(double seconds);
    void setPlaybackStatus(PlaybackStatus status);
    void setVisData(int[] bars);


    void setPresenter(Presenter presenter);
}
