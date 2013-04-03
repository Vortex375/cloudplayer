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
import de.pandaserv.music.shared.UserInfo;

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

        RootPanel.get("content").add(view);

        //check if user is already logged in
        RemoteService.getInstance().getCurrentUserInfo(new MyAsyncCallback<UserInfo>() {
            @Override
            protected void onResult(UserInfo result) {
                checkLogin(result);
            }
        });
    }

    private void checkLogin(UserInfo user) {
        if (user == null) {
            showLogin();
        } else {
            startApplication(user);
        }
    }

    private void showLogin() {
        view.showWait(false);
        view.setUsernameFocus();
    }

    private void startApplication(final UserInfo user) {
        /*
         * use runAsync() to load many application after successful login
         */
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                /*
                 * code download failed !!
                 */
                startupFailed(reason.getMessage());
            }

            @Override
            public void onSuccess() {
                /*
                 * inject external scripts
                 */
                //TODO: error handling here
                //ScriptInjector.fromUrl("../js/jquery-1.9.0.js").inject();
                //ScriptInjector.fromUrl("../js/jquery-ui-1.10.0.custom.min.js").inject();
                //ScriptInjector.fromUrl("../js/glisse.js").inject();

                /*
                 * initalize and start main application
                 */
                final MusicApp app;
                try {
                    /*
                     * initialize main application
                     * and delay the actual startup via a Timer
                     * this prevents certain DOM initialization bugs
                     */
                    app = MusicApp.create(user);
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
                new MyAsyncCallback<UserInfo>() {
                    @Override
                    protected void onResult(UserInfo result) {
                        if (result != null) {
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
