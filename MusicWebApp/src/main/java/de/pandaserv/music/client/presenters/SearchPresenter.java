package de.pandaserv.music.client.presenters;

import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.client.views.SearchView;
import de.pandaserv.music.shared.RangeResponse;
import de.pandaserv.music.shared.TrackDetail;

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

    @Override
    public void newSearchQuery(String query) {
        if (query.trim().equals("")) {
            view.clearResults();
        } else {
            RemoteService.getInstance().trackQuerySimple(query, new MyAsyncCallback<RangeResponse<TrackDetail>>() {
                @Override
                protected void onResult(RangeResponse<TrackDetail> result) {
                    view.setResults(result);
                }
            });
        }
    }
}
