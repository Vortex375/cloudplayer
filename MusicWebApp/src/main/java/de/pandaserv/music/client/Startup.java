package de.pandaserv.music.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import de.pandaserv.music.client.misc.JSUtil;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Startup implements EntryPoint {
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

    }

    private void startApplication(final long userId) {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                startupFailed(reason.getMessage());
            }

            @Override
            public void onSuccess() {
                final MusicApp app = new MusicApp(userId);
                new Timer() {
                    @Override
                    public void run() {
                        app.start();
                    }
                }.schedule(1);
            }
        });
    }

    private void startupFailed(String message) {
        //TODO
        Window.alert("Application startup failed: " + message);
    }
}
