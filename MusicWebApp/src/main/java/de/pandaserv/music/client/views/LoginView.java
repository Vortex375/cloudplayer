package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.IsWidget;

public interface LoginView extends IsWidget {
    public interface Presenter {
        void onLoginButtonClicked();
        void onUsernameChange();
        void onPasswordChange();
    }

    void showWait(boolean show);
    void showLoginWait(boolean show);
    void showLoginError(boolean show);
    void showUsernameError(boolean show);
    void showPasswordError(boolean show);
    void setUsernameFocus();
    void setPasswordFocus();

    String getUsername();
    String getPassword();

    void setPresenter(Presenter presenter);
}
