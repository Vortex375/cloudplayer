package de.pandaserv.music.client.presenters;

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

    public MenuPresenter(MenuView view) {
        this.view = view;
    }
}
