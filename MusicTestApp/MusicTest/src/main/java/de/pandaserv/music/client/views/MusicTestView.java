package de.pandaserv.music.client.views;

import com.google.gwt.dom.client.AudioElement;
import de.pandaserv.music.shared.PlaybackStatus;

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
    }

    void showError(boolean show);
    void setErrorMessage(String message);

    void setDuration(double seconds);
    void setTime(double seconds);
    void setPlaybackStatus(PlaybackStatus status);

    void setDebugString(String debug);

    AudioElement getAudioElement();

    void setPresenter(Presenter presenter);
}
