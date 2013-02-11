package de.pandaserv.music.client.presenters;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import de.pandaserv.music.client.views.MusicTestView;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestPresenter implements MusicTestView.Presenter {
    private MusicTestView view;
    private AudioElement audio;
    private long streamId;

    private Timer debugTimer;

    private static final String SERVICE_URL = Window.Location.getProtocol() + "//" + Window.Location.getHost() +
    "/service/stream/";

    public MusicTestPresenter(MusicTestView view) {
        this.view = view;

        audio = view.getAudioElement();

        debugTimer = new Timer() {
            @Override
            public void run() {
                updateDebug();
            }
        };

        if (audio == null) {
            view.setErrorMessage("This browser does not support the Audio element.");
            view.showError(true);
        } else {
            audio.setAutoplay(false);
            debugTimer.scheduleRepeating(500);
        }
    }

    public void setStreamId(long id) {
        this.streamId = id;
        audio.setSrc(SERVICE_URL + id);
        audio.setPreload(MediaElement.PRELOAD_AUTO);
        audio.load();
    }



    private void updateDebug() {
        String readyState;
        switch(audio.getReadyState()) {
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
        switch (audio.getNetworkState()) {
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
    public void play() {
        audio.play();
    }
}
