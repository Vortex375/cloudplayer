package de.pandaserv.music.client.presenters;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.views.MusicTestView;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestPresenter implements MusicTestView.Presenter {
    private MusicTestView view;
    private AudioElement audioElement;
    private AudioSystem audioSystem;
    private long streamId;

    //private Timer debugTimer;

    private PlaybackStatus playbackStatus;

    private static final String SERVICE_URL = Window.Location.getProtocol() + "//" + Window.Location.getHost() +
    "/service/stream/";

    public MusicTestPresenter(final MusicTestView view) {
        this.view = view;

        audioElement = view.getAudioElement();

        Timer debugTimer = new Timer() {
            @Override
            public void run() {
                updateDebug();
            }
        };

        if (audioElement == null) {
            view.setErrorMessage("This browser does not support the Audio element.");
            view.showError(true);
        } else {
            try {
                audioSystem = new AudioSystem(audioElement);
                audioSystem.addVisDataHandler(new AudioSystem.VisDataHandler() {
                    @Override
                    public void onVisDataUpdate(int[] data) {
                        view.setVisData(data);
                    }
                });
            } catch (NotSupportedException e) {
                audioSystem = null;
            }

            audioElement.setAutoplay(false);
            bind(audioElement);
            //debugTimer.scheduleRepeating(250);
        }
    }

    private native void bind(MediaElement element) /*-{
        var that = this;
        element.addEventListener("play", function () {
            that.@de.pandaserv.music.client.presenters.MusicTestPresenter::onPlay()();
        });
        element.addEventListener("pause", function () {
            that.@de.pandaserv.music.client.presenters.MusicTestPresenter::onPause()();
        });
        element.addEventListener("durationchange", function () {
            that.@de.pandaserv.music.client.presenters.MusicTestPresenter::onDurationChange()();
        });
        element.addEventListener("timeupdate", function () {
            that.@de.pandaserv.music.client.presenters.MusicTestPresenter::onTimeUpdate()();
        });
    }-*/;

    public void setStreamId(long id) {
        this.streamId = id;
        audioElement.setSrc(SERVICE_URL + id);
        audioElement.setPreload(MediaElement.PRELOAD_AUTO);
        audioElement.load();
    }



    private void updateDebug() {
        String readyState;
        switch(audioElement.getReadyState()) {
            case (MediaElement.HAVE_NOTHING):
                readyState = "HAVE_NOTHING";
                break;
            case (MediaElement.HAVE_METADATA):
                readyState = "HAVE_METADATA";
                break;
            case (MediaElement.HAVE_CURRENT_DATA):
                readyState = "HAVE_CURRENT_DATA";
                break;
            case (MediaElement.HAVE_ENOUGH_DATA):
                readyState = "HAVE_ENOUGH_DATA";
                break;
            case (MediaElement.HAVE_FUTURE_DATA):
                readyState = "HAVE_FUTURE_DATA";
                break;
            default:
                readyState = "UNKNOWN";
                break;
        }
        String networkState;
        switch (audioElement.getNetworkState()) {
            case (MediaElement.NETWORK_EMPTY):
                networkState = "NETWORK_EMPTY";
                break;
            case (MediaElement.NETWORK_IDLE):
                networkState = "NETWORK_IDLE";
                break;
            case (MediaElement.NETWORK_LOADING):
                networkState = "NETWORK_LOADING";
                break;
            case (MediaElement.NETWORK_NO_SOURCE):
                networkState = "NETWORK_NO_SOURCE";
                break;
            default:
                networkState = "UNKNOWN";
                break;
        }

        view.setDebugString("readyState: " + readyState + " / networkState: " + networkState);
    }

    @Override
    public void playToggle() {
        if (playbackStatus == PlaybackStatus.PLAY) {
            audioElement.pause();
        } else {
            audioElement.play();
        }
    }

    @Override
    public void seekTo(double seconds) {
        audioElement.setCurrentTime(seconds);
    }

    void onPlay() {
        playbackStatus = PlaybackStatus.PLAY;
        if (audioSystem != null) {
            audioSystem.startCollectVisData();
        }
        view.setPlaybackStatus(playbackStatus);
    }

    void onPause() {
        playbackStatus = PlaybackStatus.PAUSE;
        if (audioSystem != null) {
            audioSystem.stopCollectVisData();
        }
        view.setPlaybackStatus(playbackStatus);
    }

    void onDurationChange() {
        view.setDuration(audioElement.getDuration());
    }

    void onTimeUpdate() {
        view.setTime(audioElement.getCurrentTime());
    }
}
