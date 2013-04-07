package de.pandaserv.music.client.presenters;

import de.pandaserv.music.client.views.SearchView;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/7/13
 * Time: 5:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchPresenter implements SearchView.Presenter {
    private SearchView view;

    public SearchPresenter(SearchView view) {
        this.view = view;
    }
}
