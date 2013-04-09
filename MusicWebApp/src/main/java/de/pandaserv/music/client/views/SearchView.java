package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
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

        void onTrackClicked(long id);
    }

    String getQueryString();

    void clearResults();
    void setResults(RangeResponse<TrackDetail> results);

    void setPresenter(Presenter presenter);
}
