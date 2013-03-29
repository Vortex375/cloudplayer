package de.pandaserv.music.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import de.pandaserv.music.client.control.PlaybackController;
import de.pandaserv.music.client.control.PlaybackControllerImpl;
import de.pandaserv.music.client.misc.NotSupportedException;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MusicApp {
    public static final String STREAM_SERVICE_URL = GWT.getHostPageBaseURL() + "/service/stream/";

    private final long userId;
    private final SimpleEventBus eventBus;
    private final PlaybackControllerImpl playbackController;

    //SINGLETON
    private static MusicApp INSTANCE;

    public static MusicApp create(long userId) throws NotSupportedException {
        if (INSTANCE == null) {
            INSTANCE = new MusicApp(userId);
        } else {
            throw new IllegalStateException("You can only create one instance of MusicApp!");
        }

        return INSTANCE;
    }

    public static MusicApp getInstance() {
        return INSTANCE;
    }

    private MusicApp(long userId) throws NotSupportedException {
        this.userId = userId;

        /* initialize application infrastructure */
        eventBus = new SimpleEventBus();
        playbackController = new PlaybackControllerImpl(eventBus);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public PlaybackController getPlaybackController() {
        return playbackController;
    }

    public void start() {
        Window.alert("App startup complete!");
    }
}
