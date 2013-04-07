package de.pandaserv.music.client.presenters;

import de.pandaserv.music.client.views.WelcomeView;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class WelcomePresenter implements WelcomeView.Presenter {
    private WelcomeView view;

    public WelcomePresenter(WelcomeView view) {
        this.view = view;
    }
}
