package de.pandaserv.music.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import de.pandaserv.music.client.activities.MyActivityMapper;
import de.pandaserv.music.client.control.PlaybackController;
import de.pandaserv.music.client.control.PlaybackControllerImpl;
import de.pandaserv.music.client.events.LoginEvent;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.places.AppPlaceHistoryMapper;
import de.pandaserv.music.client.places.WelcomePlace;
import de.pandaserv.music.client.presenters.MainPresenter;
import de.pandaserv.music.client.presenters.MenuPresenter;
import de.pandaserv.music.client.presenters.PlaybackPresenter;
import de.pandaserv.music.client.views.*;
import de.pandaserv.music.shared.UserInfo;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MusicApp {
    public static final String STREAM_SERVICE_URL = GWT.getHostPageBaseURL() + "/service/stream/";

    private final UserInfo user;
    private final SimpleEventBus eventBus;
    private PlaceController placeController;
    private PlaceHistoryHandler historyHandler;
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

        placeController = new PlaceController(eventBus);
        historyHandler = new PlaceHistoryHandler(new AppPlaceHistoryMapper());
        historyHandler.register(placeController, eventBus, new WelcomePlace());
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

    public PlaceController getPlaceController() {
        return placeController;
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
         * MenuView and Presenter
         */
        MenuView menuView = new MenuViewImpl();
        MenuPresenter menuPresenter = new MenuPresenter(menuView);
        menuView.setPresenter(menuPresenter);
        mainView.getMenuColumn().setWidget(menuView);

        /*
         * Main Area ActivityManager
         */
        ActivityManager activityManager = new ActivityManager(new MyActivityMapper(), eventBus);
        activityManager.setDisplay(mainView.getMainColumn());

        /*
         * remove the login view and add main panel
         */
        RootPanel.get("content").clear();
        RootPanel.get("content").add(mainPanel);

        /*
         * add resize handler for main content and do initial resize
         */
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                resizeMainContent();
            }
        });
        resizeMainContent();

        /*
         * after everything is set up, fire a login event
         * and go to the current place
         */
        eventBus.fireEvent(new LoginEvent(getUser()));
        historyHandler.handleCurrentHistory();
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
