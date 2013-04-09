package de.pandaserv.music.client.presenters;

import de.pandaserv.music.client.MusicApp;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.events.*;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.client.views.PlaybackView;
import de.pandaserv.music.shared.Track;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/3/13
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackPresenter implements PlaybackView.Presenter {

    private PlaybackView view;

    public PlaybackPresenter(final PlaybackView view) {
        this.view = view;

        /*
         * register handlers for various playback-related events
         */
        MusicApp.getInstance().getEventBus().addHandler(PlaybackCurrentIdEvent.TYPE, new PlaybackCurrentIdEventHandler() {
            @Override
            public void onCurrentId(long id) {
                refreshCurrentTrackInfo(id);
            }
        });
        MusicApp.getInstance().getEventBus().addHandler(PlaybackDurationEvent.TYPE, new PlaybackDurationEventHandler() {
            @Override
            public void onDuration(double duration) {
                view.setDuration(duration);
            }
        });
        MusicApp.getInstance().getEventBus().addHandler(PlaybackTimeEvent.TYPE, new PlaybackTimeEventHandler() {
            @Override
            public void onTime(double time) {
                view.setTime(time);
            }
        });

        MusicApp.getInstance().getEventBus().addHandler(PlaybackStatusEvent.TYPE, new PlaybackStatusEventHandler() {
            @Override
            public void onPlaybackStatus(PlaybackStatus status) {
                view.setPlaybackStatus(status);
            }
        });
        MusicApp.getInstance().getEventBus().addHandler(PlaybackWaitingEvent.TYPE, new PlaybackWaitingEventHandler() {
            @Override
            public void onPlaybackWaiting(boolean waiting) {
                view.showPlaybackWaiting(waiting);
            }
        });

        MusicApp.getInstance().getPlaybackController().addVisDataHandler(new AudioSystem.VisDataHandler() {
            @Override
            public void onVisDataUpdate(int[] data) {
                view.setVisData(data);
            }
        });
    }

    private void refreshCurrentTrackInfo(long id) {
        RemoteService.getInstance().getTrackInfo(id, new MyAsyncCallback<Track>() {
            @Override
            protected void onResult(Track result) {
                view.setCurrentTrackInfo(result);
            }
        });
    }

    @Override
    public void playToggle() {
        MusicApp.getInstance().getPlaybackController().playToggle();
    }

    @Override
    public void seekTo(double seconds) {
        MusicApp.getInstance().getPlaybackController().seekTo(seconds);
    }
}
