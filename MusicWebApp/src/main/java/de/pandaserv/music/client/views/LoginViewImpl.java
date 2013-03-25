package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

public class LoginViewImpl extends Composite implements LoginView {

    @UiTemplate("LoginView.ui.xml")
    interface LoginViewUiBinder extends UiBinder<HTMLPanel, LoginViewImpl> { }
    private static LoginViewUiBinder ourUiBinder = GWT.create(LoginViewUiBinder.class);

    private Presenter presenter;

    @UiField
    TextBox usernameBox;
    @UiField
    PasswordTextBox passwordBox;
    @UiField
    Button loginButton;
    @UiField
    Button demoButton;
    @UiField
    Alert loginError;
    @UiField
    ControlGroup usernameControl;
    @UiField
    ControlGroup passwordControl;
    @UiField
    HelpInline usernameHelp;
    @UiField
    HelpInline passwordHelp;

    public LoginViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showDemoLogin(boolean show) {
        demoButton.setVisible(show);
    }

    @Override
    public void showWait(boolean show) {
        if (show) {
            /*
             * Remove focus from input fields on submit. This closes the on-screen keyboard on mobile devices.
             */
            usernameBox.setFocus(false);
            passwordBox.setFocus(false);
            loginButton.state().loading();
            demoButton.setEnabled(false);
        } else {
            loginButton.state().reset();
            demoButton.setEnabled(true);
        }
    }

    @Override
    public void showLoginError(boolean show) {
        loginError.setVisible(show);
        if (show) {
            setUsernameFocus();
        }
    }

    @Override
    public void showUsernameError(boolean show) {
        if (show) {
            usernameControl.setType(ControlGroupType.ERROR);
            usernameHelp.removeStyleName("phantom");
            setUsernameFocus();
        } else {
            usernameControl.setType(ControlGroupType.NONE);
            usernameHelp.addStyleName("phantom");
        }
    }

    @Override
    public void showPasswordError(boolean show) {
        if (show) {
            passwordControl.setType(ControlGroupType.ERROR);
            passwordHelp.removeStyleName("phantom");
            setPasswordFocus();
        } else {
            passwordControl.setType(ControlGroupType.NONE);
            passwordHelp.addStyleName("phantom");
        }
    }

    @Override
    public String getUsername() {
        return usernameBox.getText();
    }

    @Override
    public String getPassword() {
        return passwordBox.getText();
    }

    @Override
    public void setUsernameFocus() {
        usernameBox.setFocus(true);
        usernameBox.selectAll();
    }

    @Override
    public void setPasswordFocus() {
        passwordBox.setFocus(true);
        passwordBox.selectAll();
    }

    @UiHandler("usernameBox")
    void handleUsernameChange(ValueChangeEvent<String> event) {
        presenter.onUsernameChange();
    }

    @UiHandler("usernameBox")
    void handleUsernameReturn(KeyPressEvent event) {
        /*
         * on "Return" key in username box move focus to password box
         */
        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
            setPasswordFocus();
        }
    }

    @UiHandler("passwordBox")
    void handlePasswordReturn(KeyPressEvent event) {
        /*
         * on "Return" key in password box submit form
         */
        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
            presenter.onLoginButtonClicked();
        }
    }

    @UiHandler("passwordBox")
    void handlePasswordChange(ValueChangeEvent<String> event) {
        presenter.onPasswordChange();
    }

    @UiHandler("loginButton")
    void handleLoginClick(ClickEvent event) {
        presenter.onLoginButtonClicked();
    }

    @UiHandler("demoButton")
    void handleDebugClick(ClickEvent event) {
        usernameBox.setFocus(false);
        passwordBox.setFocus(false);
        presenter.onDemoLoginButtonClicked();
    }
}