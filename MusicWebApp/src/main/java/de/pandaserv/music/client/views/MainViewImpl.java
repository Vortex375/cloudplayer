package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/3/13
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainViewImpl extends Composite implements MainView {
    @UiTemplate("MainView.ui.xml")
    interface MainViewUiBinder extends UiBinder<HTMLPanel, MainViewImpl> {

    }
    private static MainViewUiBinder ourUiBinder = GWT.create(MainViewUiBinder.class);

    private Presenter presenter;

    @UiField
    NavLink userNameLabel;
    @UiField
    NavLink logoutButton;
    @UiField
    NavLink messagesButton;
    @UiField
    SimplePanel menuColumn;
    @UiField
    SimplePanel mainColumn;

    public MainViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));
        addStyleName("mainView");
    }

    @Override
    public AcceptsOneWidget getMenuColumn() {
        return menuColumn;
    }

    @Override
    public AcceptsOneWidget getMainColumn() {
        return mainColumn;
    }

    @Override
    public void setUsername(String username) {
        userNameLabel.setText(username);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("logoutButton")
    void onLogoutButtonClicked(ClickEvent e) {
        presenter.onLogoutButtonClicked();
    }

    @UiHandler("messagesButton")
    void onMessagesButtonClicked(ClickEvent e) {
        presenter.onMessagesClicked();
    }
}