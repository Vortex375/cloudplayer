package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import de.pandaserv.music.client.places.AdminPlace;
import de.pandaserv.music.client.places.SearchPlace;
import de.pandaserv.music.client.places.WelcomePlace;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class MenuViewImpl extends Composite implements MenuView {
    @UiTemplate("MenuView.ui.xml")
    interface MenuViewUiBinder extends UiBinder<HTMLPanel, MenuViewImpl> {

    }
    private static MenuViewUiBinder ourUiBinder = GWT.create(MenuViewUiBinder.class);

    private Presenter presenter;

    @UiField
    NavLink homeButton;
    @UiField
    NavLink searchButton;
    @UiField
    NavLink adminButton;

    public MenuViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public void setCurrentPlace(Place place) {
        //TODO: all buttons
        homeButton.setActive(false);
        searchButton.setActive(false);
        adminButton.setActive(false);
        if (place instanceof WelcomePlace) {
            homeButton.setActive(true);
        } else if (place instanceof SearchPlace) {
            searchButton.setActive(true);
        } else if (place instanceof AdminPlace) {
            adminButton.setActive(true);
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("homeButton")
    void onHomeButtonClicked(ClickEvent e) {
        presenter.onHomeButtonClicked();
    }

    @UiHandler("searchButton")
    void onSearchButtonClicked(ClickEvent e) {
        presenter.onSearchButtonClicked();
    }

    @UiHandler("adminButton")
    void onAdminButtonClicked(ClickEvent e) {
        presenter.onAdminButtonClicked();
    }
}
