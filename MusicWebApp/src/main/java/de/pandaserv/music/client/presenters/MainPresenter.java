package de.pandaserv.music.client.presenters;

import com.google.gwt.user.client.Window;
import de.pandaserv.music.client.MusicApp;
import de.pandaserv.music.client.events.LoginEvent;
import de.pandaserv.music.client.events.LoginEventHandler;
import de.pandaserv.music.client.events.LogoutEvent;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.client.views.MainView;
import de.pandaserv.music.shared.UserInfo;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/3/13
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainPresenter implements MainView.Presenter {

    private MainView view;

    public MainPresenter(final MainView view) {
        this.view = view;

        MusicApp.getInstance().getEventBus().addHandler(LoginEvent.TYPE, new LoginEventHandler() {
            @Override
            public void onLogin(UserInfo user) {
                view.setUsername(user.getName());
            }
        });
    }

    @Override
    public void onLogoutButtonClicked() {
        MusicApp.getInstance().getEventBus().fireEvent(new LogoutEvent());
        /*
         * "log out" by reloading the application
         */
        // TODO: this could be done more elegantly
        RemoteService.getInstance().logout(new MyAsyncCallback<Void>() {
            @Override
            protected void onResult(Void result) {
                Window.Location.reload();
            }
        });
    }

    @Override
    public void onMessagesClicked() {
        // TODO
    }
}
