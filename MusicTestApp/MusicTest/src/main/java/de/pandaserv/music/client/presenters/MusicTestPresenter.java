package de.pandaserv.music.client.presenters;

import de.pandaserv.music.client.views.MusicTestView;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestPresenter implements MusicTestView.Presenter {
    private MusicTestView view;

    public MusicTestPresenter(MusicTestView view) {
        this.view = view;
    }
}
