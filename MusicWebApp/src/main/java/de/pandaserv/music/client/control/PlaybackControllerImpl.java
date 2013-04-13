package de.pandaserv.music.client.control;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.media.client.Audio;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.MusicApp;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.events.PlaybackCurrentIdEvent;
import de.pandaserv.music.client.events.PlaybackDurationEvent;
import de.pandaserv.music.client.events.PlaybackStatusEvent;
import de.pandaserv.music.client.events.PlaybackTimeEvent;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.shared.QueueMode;
import de.pandaserv.music.shared.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackControllerImpl implements PlaybackController {

    private AudioElement audioElement;
    private AudioSystem audioSystem;
    private EventBus eventBus;

    private PlaybackStatus playbackStatus = PlaybackStatus.STOP;
    private long currentId = -1;
    private QueueMode queueMode;
    private long[] queue;
    private int queuePosition;

    private int seekOffset = 0;

    private List<AudioSystem.VisDataHandler> handlers;

    public PlaybackControllerImpl(final EventBus eventBus) throws NotSupportedException {
        this.eventBus = eventBus;

        handlers = new ArrayList<AudioSystem.VisDataHandler>();

        boolean enableAudioSystem = Boolean.parseBoolean(Dictionary.getDictionary("startupConfig").get("enableVis"));

        /*
         * create Audio playback element
         */
        Audio audio = Audio.createIfSupported();
        if (audio != null) {
            audioElement = audio.getAudioElement();
        } else {
            throw new NotSupportedException();
        }

        /*
         * create AudioSystem (for vis), if enabled
         */
        if (enableAudioSystem) {
            try {
                audioSystem = new AudioSystem();
                audioSystem.addVisDataHandler(new AudioSystem.VisDataHandler() {
                    @Override
                    public void onVisDataUpdate(int[] data) {
                        for (AudioSystem.VisDataHandler handler: handlers) {
                            handler.onVisDataUpdate(data);
                        }
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

    private native void bind(MediaElement element) /*-{
        var that = this;
        element.addEventListener("play", function () {
            that.@de.pandaserv.music.client.control.PlaybackControllerImpl::onPlay()();
        });
        element.addEventListener("pause", function () {
            that.@de.pandaserv.music.client.control.PlaybackControllerImpl::onPause()();
        });
        element.addEventListener("timeupdate", function () {
            that.@de.pandaserv.music.client.control.PlaybackControllerImpl::onTimeUpdate()();
        });
    }-*/;

    void onPlay() {
        playbackStatus = PlaybackStatus.PLAY;
        if (audioSystem != null) {
            audioSystem.startCollectVisData();
        }

        eventBus.fireEvent(new PlaybackStatusEvent(PlaybackStatus.PLAY));
    }

    void onPause() {
        playbackStatus = PlaybackStatus.PAUSE;
        if (audioSystem != null) {
            audioSystem.stopCollectVisData();
        }

        eventBus.fireEvent(new PlaybackStatusEvent(PlaybackStatus.PAUSE));
    }

    void onTimeUpdate() {
        eventBus.fireEvent(new PlaybackTimeEvent(seekOffset + audioElement.getCurrentTime()));
    }

    @Override
    public void play() {
        audioElement.play();
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
    public void pause() {
        audioElement.pause();
    }

    @Override
    public void seekTo(double seconds) {
        PlaybackStatus oldStatus = playbackStatus;
        seekOffset = (int) seconds;
        initAudioElement();
        if (oldStatus == PlaybackStatus.PLAY) {
            play();
        }
    }

    @Override
    public PlaybackStatus getPlaybackStatus() {
        return playbackStatus;
    }

    @Override
    public long getCurrentId() {
        return currentId;
    }

    @Override
    public QueueMode getQueueMode() {
        return queueMode;
    }

    @Override
    public long[] getQueue() {
        return queue;
    }

    @Override
    public int getQueuePosition() {
        return queuePosition;
    }

    @Override
    public void setQueue(QueueMode queueMode, long[] queue, int pos) {
        this.queueMode = queueMode;
        this.queue = queue;
        this.queuePosition = pos;
        this.seekOffset = 0;

        tickle();
    }

    @Override
    public void setQueuePosition(int position) {
        if (position < 0 || position >= queue.length) {
            throw new IllegalArgumentException("position out of range");
        }
        this.queuePosition = position;
        this.seekOffset = 0;

        tickle();
    }

    @Override
    public HandlerRegistration addVisDataHandler(final AudioSystem.VisDataHandler handler) {
        handlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                handlers.remove(handler);
            }
        };
    }

    private void initAudioElement() {
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
        audioElement.setSrc(MusicApp.STREAM_SERVICE_URL + currentId + "?offset=" + seekOffset);
        audioElement.pause();
        audioElement.load();
        if (audioSystem != null) {
            audioSystem.connect();
        }
    }

    // actually move to the next track
    private void tickle() {
        currentId = queue[queuePosition];
        eventBus.fireEvent(new PlaybackCurrentIdEvent(currentId));

        getCurrentTrackDuration();

        //eventBus.fireEvent(new PlaybackWaitingEvent(false));
        /*
         * start playback of next track
         */
        initAudioElement();
        play();
    }

    private void getCurrentTrackDuration() {
        RemoteService.getInstance().getTrackInfo(currentId, new MyAsyncCallback<Track>() {
            @Override
            protected void onResult(Track result) {
                eventBus.fireEvent(new PlaybackDurationEvent(result.getDuration()));
            }
        });
    }
}
