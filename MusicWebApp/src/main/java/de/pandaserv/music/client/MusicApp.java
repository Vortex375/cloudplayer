package de.pandaserv.music.client;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MusicApp {
    public static final String STREAM_SERVICE_URL = GWT.getHostPageBaseURL() + "/service/stream/";

    private final long userId;
    private final SimpleEventBus eventBus;

    //SINGLETON
    private static MusicApp INSTANCE;

    public static MusicApp create(long userId) {
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

    private MusicApp(long userId) {
        this.userId = userId;

        /* initialize application infrastructure */
        eventBus = new SimpleEventBus();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void start() {

    }
}
