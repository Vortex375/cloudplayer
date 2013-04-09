package de.pandaserv.music.client.activities;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import de.pandaserv.music.client.presenters.SearchPresenter;
import de.pandaserv.music.client.views.SearchView;
import de.pandaserv.music.client.views.SearchViewImpl;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 11:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchActivity extends MyActivity implements CacheableActivity {

    private SearchView view;
    private SearchPresenter presenter;

    public SearchActivity() {
        view = new SearchViewImpl();
        presenter = new SearchPresenter(view);
        view.setPresenter(presenter);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
    }
}
