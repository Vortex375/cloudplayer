package de.pandaserv.music.client.presenters;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.script.javascript.JSAdapter;
import de.pandaserv.music.client.MusicTest;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.misc.JSUtil;
import de.pandaserv.music.client.views.MusicTestView;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.shared.FileStatus;
import de.pandaserv.music.shared.Track;
import de.pandaserv.music.shared.TrackDetail;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestPresenter implements MusicTestView.Presenter {
    private static final boolean ENABLE_AUDIO_SYSTEM = true;

    private MusicTestView view;
    private AudioElement audioElement;
    private AudioSystem audioSystem;
    private long streamId;
    private long watchId;
    private int waitIndex;
    private long[] queryResultIds;
    private Timer watchTimer;

    //private Timer debugTimer;

    private PlaybackStatus playbackStatus;

    private static final String SERVICE_URL = Window.Location.getProtocol() + "//" + Window.Location.getHost() +
            "/service/stream/";

    public MusicTestPresenter(final MusicTestView view) {
        this.view = view;

        watchTimer = new Timer() {
            @Override
            public void run() {
                checkFileStatus();
            }
        };
        waitIndex = -1;

        Audio audio = Audio.createIfSupported();
        if (audio != null) {
            audioElement = audio.getAudioElement();
        } else {
            audioElement = null;
        }

        if (audioElement == null) {
            view.setErrorMessage("This browser does not support the Audio element.");
            view.showError(true);
        } else {
            if (ENABLE_AUDIO_SYSTEM) {
                try {
                    audioSystem = new AudioSystem();
                    audioSystem.addVisDataHandler(new AudioSystem.VisDataHandler() {
                        @Override
                        public void onVisDataUpdate(int[] data) {
                            view.setVisData(data);
                        }
                    });
                } catch (NotSupportedException e) {
                    audioSystem = null;
                }
            } else {
                audioSystem = null;
            }
            bind(audioElement);
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

    public void setStreamId(final long id) {
        this.streamId = id;
        if (audioSystem != null) {
            audioSystem.disconnect();
            audioElement.setSrc("");
            audioElement.pause();
            audioElement.load();
            /*
             * Create a new audio element !!
             * This _should_ not be necessary but it is needed
             * to work around an audio playback bug
             */
            audioElement = Audio.createIfSupported().getAudioElement();
            bind(audioElement); //TODO: unbind old element?
            audioSystem.setMediaElement(audioElement);
        }
        audioElement.setSrc(SERVICE_URL + id);
        audioElement.pause();
        audioElement.load();
        if (audioSystem != null) {
            audioSystem.connect();
        }

        MusicTest.getService().getTrackInfo(id, new AsyncCallback<Track>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(Track track) {
                view.setCurrentTrackInfo(track);
            }
        });
    }

    private void checkFileStatus() {
        watchTimer.cancel();
        MusicTest.getService().getStatus(watchId, new AsyncCallback<FileStatus>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(FileStatus fileStatus) {
                if (fileStatus == FileStatus.PREPARED) {
                    if (waitIndex >= 0) {
                        view.showWaitOnResult(waitIndex, false);
                    }
                    setStreamId(watchId);
                    audioElement.play();
                } else {
                    view.showWaitOnResult(waitIndex, true);
                    watchTimer.schedule(500);
                }
            }
        });
    }

    private void updateDebug() {
        String readyState;
        switch (audioElement.getReadyState()) {
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
    public void newSearchQuery() {
        if (waitIndex >= 0) {
            view.showWaitOnResult(waitIndex, false);
            waitIndex = -1;
        }
        MusicTest.getService().trackQuerySimple(view.getSearchQuery(), new AsyncCallback<TrackDetail[]>() {
            @Override
            public void onFailure(Throwable throwable) {
                view.showError(true);
                view.setErrorMessage("search query failed");
            }

            @Override
            public void onSuccess(TrackDetail[] results) {
                queryResultIds = new long[results.length];
                for (int i = 0; i < results.length; i++) {
                    queryResultIds[i] = results[i].getId();
                }
                view.setSearchResults(results);
            }
        });
    }

    @Override
    public void onSearchResultClicked(int index) {
        if (waitIndex >= 0) {
            view.showWaitOnResult(waitIndex, false);
        }
        watchId = queryResultIds[index];
        waitIndex = index;
        MusicTest.getService().prepare(watchId, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void aVoid) {
            }
        });
        checkFileStatus();
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
