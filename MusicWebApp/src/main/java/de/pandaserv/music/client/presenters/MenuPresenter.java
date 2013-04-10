package de.pandaserv.music.client.presenters;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.Event;
import de.pandaserv.music.client.MusicApp;
import de.pandaserv.music.client.places.AdminPlace;
import de.pandaserv.music.client.places.SearchPlace;
import de.pandaserv.music.client.places.WelcomePlace;
import de.pandaserv.music.client.views.MenuView;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class MenuPresenter implements MenuView.Presenter {
    private MenuView view;

    public MenuPresenter(final MenuView view) {
        this.view = view;

        MusicApp.getInstance().getEventBus().addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
            @Override
            public void onPlaceChange(PlaceChangeEvent placeChangeEvent) {
                view.setCurrentPlace(placeChangeEvent.getNewPlace());
            }
        });

        /*
         * Handler for menu keyboard shortcuts
         */
        Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(Event.NativePreviewEvent preview) {
                NativeEvent ne = preview.getNativeEvent();
                if (ne.getCtrlKey() && ne.getKeyCode() == '0') {
                    onHomeButtonClicked();
                } else if (ne.getCtrlKey() && ne.getKeyCode() == '1') {
                    onSearchButtonClicked();
                }
            }
        });
    }

    @Override
    public void onHomeButtonClicked() {
        MusicApp.getInstance().getPlaceController().goTo(new WelcomePlace());
    }

    @Override
    public void onSearchButtonClicked() {
        MusicApp.getInstance().getPlaceController().goTo(new SearchPlace());
    }

    @Override
    public void onAdminButtonClicked() {
        MusicApp.getInstance().getPlaceController().goTo(new AdminPlace());
    }
}
