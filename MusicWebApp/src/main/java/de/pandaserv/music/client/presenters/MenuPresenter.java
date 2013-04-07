package de.pandaserv.music.client.presenters;

import com.google.gwt.place.shared.PlaceChangeEvent;
import de.pandaserv.music.client.MusicApp;
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
    }

    @Override
    public void onHomeButtonClicked() {
        MusicApp.getInstance().getPlaceController().goTo(new WelcomePlace());
    }

    @Override
    public void onSearchButtonClicked() {
        MusicApp.getInstance().getPlaceController().goTo(new SearchPlace());
    }
}
