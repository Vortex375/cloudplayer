package de.pandaserv.music.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import de.pandaserv.music.client.control.PlaybackController;
import de.pandaserv.music.client.control.PlaybackControllerImpl;
import de.pandaserv.music.client.events.LoginEvent;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.presenters.MainPresenter;
import de.pandaserv.music.client.presenters.PlaybackPresenter;
import de.pandaserv.music.client.views.MainView;
import de.pandaserv.music.client.views.MainViewImpl;
import de.pandaserv.music.client.views.PlaybackView;
import de.pandaserv.music.client.views.PlaybackViewImpl;
import de.pandaserv.music.shared.UserInfo;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MusicApp {
    public static final String STREAM_SERVICE_URL = GWT.getHostPageBaseURL() + "/service/stream/";

    private final UserInfo user;
    private final SimpleEventBus eventBus;
    private final PlaybackControllerImpl playbackController;

    private MainView mainView;
    private PlaybackView playbackView;

    //SINGLETON
    private static MusicApp INSTANCE;

    public static MusicApp create(UserInfo user) throws NotSupportedException {
        if (INSTANCE == null) {
            INSTANCE = new MusicApp(user);
        } else {
            throw new IllegalStateException("You can only create one instance of MusicApp!");
        }

        return INSTANCE;
    }

    public static MusicApp getInstance() {
        return INSTANCE;
    }

    private MusicApp(UserInfo user) throws NotSupportedException {
        /*
         * initialize application infrastructure
         */
        this.user = user;
        eventBus = new SimpleEventBus();
        playbackController = new PlaybackControllerImpl(eventBus);
    }

    public UserInfo getUser() {
        return user;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public PlaybackController getPlaybackController() {
        return playbackController;
    }

    public void start() {
        /*
         * set up the GUI
         */

        /*
         * MainView and Presenter (NavBar, Menu etc.)
         */
        mainView = new MainViewImpl();
        MainPresenter mainPresenter = new MainPresenter(mainView);
        mainView.setPresenter(mainPresenter);

        /*
         * PlaybackView and Presenter (current track info, playback controls, album art, visualization)
         */
        playbackView = new PlaybackViewImpl();
        PlaybackPresenter playbackPresenter = new PlaybackPresenter(playbackView);
        playbackView.setPresenter(playbackPresenter);

        /*
         * lay out the main panel
         */
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.add(mainView);
        mainPanel.add(playbackView);

        /*
         * remove the login view and add main panel
         */
        RootPanel.get("content").clear();
        RootPanel.get("content").add(mainPanel);

        /*
         * add resize handler for main content
         */
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                resizeMainContent();
            }
        });
        resizeMainContent();

        /*new Timer() {
            @Override
            public void run() {
                resizeMainContent();
            }
        }.schedule(1);*/

        /*
         * after everything is set up, fire a login event
         */
        eventBus.fireEvent(new LoginEvent(getUser()));
    }

    /*
     * DockLayoutPanel sucks - this hack works better
     */
    private void resizeMainContent() {
        int height = Window.getClientHeight();
        height -= playbackView.asWidget().getOffsetHeight();
        height -= 4; // :-/
        if (height < 10) {
            height = 10;
        }
        mainView.asWidget().setHeight(height + "px");
    }
}
