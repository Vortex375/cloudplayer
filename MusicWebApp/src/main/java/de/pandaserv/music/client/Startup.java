package de.pandaserv.music.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import de.pandaserv.music.client.misc.JSUtil;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.client.views.LoginView;
import de.pandaserv.music.client.views.LoginViewImpl;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Startup implements EntryPoint, LoginView.Presenter {
    private LoginView view;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            @Override
            public void onUncaughtException(Throwable e) {
                JSUtil.log("uncaught: " + e.getMessage());
                e.printStackTrace();
            }
        });


        view = new LoginViewImpl();
        view.setPresenter(this);
        view.showWait(true);

        RootPanel.get().add(view);

        //check if user is already logged in
        RemoteService.getInstance().getCurrentUserId(new MyAsyncCallback<Long>() {
            @Override
            protected void onResult(Long result) {
                checkLogin(result);
            }
        });
    }

    private void checkLogin(long userId) {
        if (userId < 0) {
            showLogin();
        } else {
            startApplication(userId);
        }
    }

    private void showLogin() {
        view.showWait(false);
        view.setUsernameFocus();
    }

    private void startApplication(final long userId) {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                startupFailed(reason.getMessage());
            }

            @Override
            public void onSuccess() {
                final MusicApp app;
                try {
                    app = MusicApp.create(userId);
                    new Timer() {
                        @Override
                        public void run() {
                            app.start();
                        }
                    }.schedule(1);

                } catch (NotSupportedException e) {
                    //TODO
                    Window.alert("Your browser does not support the HTML5 <audio> element. You can not use this application without it.");
                }
            }
        });
    }

    private void startupFailed(String message) {
        //TODO
        Window.alert("Application startup failed: " + message);
    }

    /* Login Presenter Stuff */

    @Override
    public void onLoginButtonClicked() {
        String username = view.getUsername();
        String password = view.getPassword();
        boolean valid = true;

        /*
         * Check if the user has entered a username AND password before submit.
         */

        if (password.trim().length() == 0) {
            valid = false;
            view.showPasswordError(true);
        } else {
            view.showPasswordError(false);
        }

        if (username.trim().length() == 0) {
            valid = false;
            view.showUsernameError(true);
        } else {
            view.showUsernameError(false);
        }

        if (!valid) {
            /*
             * Missing username or password.
             */
            return;
        }

        /*
         * Attempt to login with the given password.
         */
        view.showLoginWait(true);
        RemoteService.getInstance().login(username, password,
                new MyAsyncCallback<Long>() {
                    @Override
                    protected void onResult(Long result) {
                        if (result >= 0) {
                            /*
                             * Successfully logged in. Start main application.
                             */
                            view.showWait(true);
                            startApplication(result);
                        } else {
                            /*
                             * Login failed due to wrong username or password.
                             */
                            view.showLoginWait(false);
                            view.showLoginError(true);
                        }
                    }
                });
    }

    @Override
    public void onUsernameChange() {
        view.showUsernameError(false);
    }

    @Override
    public void onPasswordChange() {
        view.showPasswordError(false);
    }
}
