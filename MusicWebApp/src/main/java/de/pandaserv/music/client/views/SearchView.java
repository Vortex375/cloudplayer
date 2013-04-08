package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import de.pandaserv.music.shared.RangeResponse;
import de.pandaserv.music.shared.TrackDetail;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SearchView extends IsWidget {
    public interface Presenter {
        void newSearchQuery(String query);
    }

    String getQueryString();

    void clearResults();
    void setResults(RangeResponse<TrackDetail> results);

    void setPresenter(Presenter presenter);
}
