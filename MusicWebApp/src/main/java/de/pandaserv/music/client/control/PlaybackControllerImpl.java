package de.pandaserv.music.client.control;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.media.client.Audio;
import com.google.web.bindery.event.shared.EventBus;
import de.pandaserv.music.client.MusicApp;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.events.*;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.shared.QueueMode;

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

    private PlaybackStatus playbackStatus;
    private long currentId;
    private QueueMode queueMode;
    private long[] queue;
    private int queuePosition;

    public PlaybackControllerImpl() throws NotSupportedException {
        eventBus = MusicApp.getInstance().getEventBus();

        boolean enableAudioSystem = Boolean.parseBoolean(Dictionary.getDictionary("startupConfig").get("enableVis"));

        Audio audio = Audio.createIfSupported();
        if (audio != null) {
            audioElement = audio.getAudioElement();
        } else {
            throw new NotSupportedException();
        }

        if (enableAudioSystem) {
            try {
                audioSystem = new AudioSystem();
                audioSystem.addVisDataHandler(new AudioSystem.VisDataHandler() {
                    @Override
                    public void onVisDataUpdate(int[] data) {
                        eventBus.fireEvent(new VisDataEvent(data));
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
        element.addEventListener("durationchange", function () {
            that.@de.pandaserv.music.client.control.PlaybackControllerImpl::onDurationChange()();
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

    void onDurationChange() {
        eventBus.fireEvent(new PlaybackDurationEvent(audioElement.getDuration()));
    }

    void onTimeUpdate() {
        eventBus.fireEvent(new PlaybackTimeEvent(audioElement.getCurrentTime()));
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
        audioElement.setCurrentTime(seconds);
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

        tickle();
    }

    @Override
    public void setQueuePosition(int position) {
        if (position < 0 || position >= queue.length) {
            throw new IllegalArgumentException("position out of range");
        }
        this.queuePosition = position;

        tickle();
    }

    // actually move to the next track
    private void tickle() {
        currentId = queue[queuePosition];

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
        audioElement.setSrc(MusicApp.STREAM_SERVICE_URL + currentId);
        audioElement.pause();
        audioElement.load();
        if (audioSystem != null) {
            audioSystem.connect();
        }

        eventBus.fireEvent(new PlaybackCurrentIdEvent(currentId));
    }
}
