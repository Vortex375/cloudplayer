package de.pandaserv.music.client.control;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.MusicApp;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.events.*;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.shared.FileStatus;
import de.pandaserv.music.shared.Priority;
import de.pandaserv.music.shared.QueueMode;

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
    private FileStatus[] statuses;
    private int queuePosition;

    private int autoPrepareIndex = 0; // current index where autoPrepare() is running, relative to queuePosition
    private long autoPrepareId; // id that is currently preparing in background
    private boolean nextTrackPreparing = false;
    private Timer autoPrepareTimer;

    private List<AudioSystem.VisDataHandler> handlers;

    public PlaybackControllerImpl(final EventBus eventBus) throws NotSupportedException {
        this.eventBus = eventBus;

        handlers = new ArrayList<AudioSystem.VisDataHandler>();

        autoPrepareTimer = new Timer() {
            @Override
            public void run() {
                autoPrepare();
            }
        };

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
        this.autoPrepareIndex = 1;
        autoPrepareTimer.cancel();
        autoPrepareTimer.schedule(2000);

        statuses = new FileStatus[queue.length];
        for (int i = 0; i < statuses.length; i++) {
            // file status is unknown - we assume NOT_PREPARED
            statuses[i] = FileStatus.NOT_PREPARED;
        }
        nextTrackPreparing = false;

        tickle();
    }

    @Override
    public void setQueuePosition(int position) {
        if (position < 0 || position >= queue.length) {
            throw new IllegalArgumentException("position out of range");
        }
        this.queuePosition = position;
        this.autoPrepareIndex = 1;
        autoPrepareTimer.cancel();
        autoPrepareTimer.schedule(2000);

        nextTrackPreparing = false;

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

    // actually move to the next track
    private void tickle() {
        currentId = queue[queuePosition];
        eventBus.fireEvent(new PlaybackCurrentIdEvent(currentId));

        /*
         * check if the next track is available
         */
        if (!(statuses[queuePosition] == FileStatus.PREPARED || statuses[queuePosition] == FileStatus.TRANSCODING)) {
            eventBus.fireEvent(new PlaybackWaitingEvent(true));
            final int queryPos = queuePosition;
            RemoteService.getInstance().getStatus(currentId, new MyAsyncCallback<FileStatus>() {
                @Override
                protected void onResult(FileStatus result) {
                    statuses[queryPos] = result;
                    if (result == FileStatus.PREPARED || result == FileStatus.TRANSCODING) {
                        /*
                         * next track is ready - run tickle() again
                         */
                        tickle();
                    } else {
                        /*
                         * next track is NOT ready - issue prepare job with HIGH priority
                         */
                        if (!nextTrackPreparing) {
                            nextTrackPreparing = true;
                            RemoteService.getInstance().prepare(currentId, Priority.HIGH, new MyAsyncCallback<Void>() {
                                @Override
                                protected void onResult(Void result) {
                                    // do nothing
                                }
                            });
                        }
                        /*
                         * check again if the track is prepared after 500ms
                         */
                        new Timer() {
                            @Override
                            public void run() {
                                tickle();
                            }
                        }.schedule(500);
                    }
                }
            });
        } else {
            eventBus.fireEvent(new PlaybackWaitingEvent(false));
            /*
             * start playback of next track
             */
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
            audioElement.play();
        }
    }

    private void autoPrepare() {
        /*
         * automatically prepare tracks on the playback queue in the background
         */
        final int queryPos = (queuePosition + autoPrepareIndex);
        if (queryPos >= queue.length) {
            // reached end of queue while auto preparing
            return;
        }
        long id = queue[queryPos];
        RemoteService.getInstance().getStatus(id, new MyAsyncCallback<FileStatus>() {
            @Override
            protected void onResult(FileStatus result) {
                statuses[queryPos] = result;
                if (result == FileStatus.PREPARED) {
                    /*
                     * we are done with this index - move to the next
                     */
                    autoPrepareIndex += 1;
                    if (autoPrepareIndex > 3) {
                        /*
                         * we have prepared the next 3 tracks
                         * stop auto prepare
                         */
                        return;
                    }
                } else {
                    if (autoPrepareId != queue[queryPos]) {
                        /*
                         * track is not preparing - issue prepare job with NORMAL priority
                         */
                        autoPrepareId = queue[queryPos];
                        RemoteService.getInstance().prepare(queue[queryPos], Priority.NORMAL, new MyAsyncCallback<Void>() {
                            @Override
                            protected void onResult(Void result) {
                                // do nothing
                            }
                        });
                    }
                }
                /*
                 * continue auto prepare
                 */
                autoPrepareTimer.schedule(2000);
            }
        });
    }
}
