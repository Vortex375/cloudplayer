package de.pandaserv.music.client.views;

public interface LoginView {
    public interface Presenter {
        void onLoginButtonClicked();
        void onUsernameChange();
        void onPasswordChange();

        void onDemoLoginButtonClicked();
    }

    void showDemoLogin(boolean show);

    void showWait(boolean show);
    void showLoginError(boolean show);
    void showUsernameError(boolean show);
    void showPasswordError(boolean show);
    void setUsernameFocus();
    void setPasswordFocus();

    String getUsername();
    String getPassword();

    void setPresenter(Presenter presenter);
}
