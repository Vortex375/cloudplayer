package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.misc.JSUtil;
import de.pandaserv.music.client.widgets.SearchResultsTable;
import de.pandaserv.music.shared.RangeResponse;
import de.pandaserv.music.shared.TrackDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchViewImpl extends Composite implements SearchView {
    @UiTemplate("SearchView.ui.xml")
    interface SearchViewUiBinder extends UiBinder<HTMLPanel, SearchViewImpl> {

    }
    private static SearchViewUiBinder ourUiBinder = GWT.create(SearchViewUiBinder.class);

    private Presenter presenter;
    private Timer autoSearchTimer;


    @UiField
    Button searchButton;
    @UiField
    Button clearButton;
    @UiField
    TextBox searchBox;
    @UiField
    Label noResultsLabel;
    @UiField
    SearchResultsTable resultsTable;

    private List<HandlerRegistration> privateHandlers;

    public SearchViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        autoSearchTimer = new Timer() {
            @Override
            public void run() {
                presenter.newSearchQuery(searchBox.getText());
            }
        };

        privateHandlers = new ArrayList<HandlerRegistration>();

        resultsTable.addTrackClickHandler(new SearchResultsTable.TrackClickHandler() {
            @Override
            public void onTrackClicked(long trackId) {
                presenter.onTrackClicked(trackId);
            }
        });

        addScrollHandler();
    }

    private native void addScrollHandler() /*-{
        var that = this;
        $wnd.$('#mainView').on('scroll', function (event) {
            that.@de.pandaserv.music.client.views.SearchViewImpl::tableFetchMore()();
        });
    }-*/;

    private void tableFetchMore() {
        Element element = Document.get().getElementById("mainView");
        JSUtil.log("scroll event: height=" + element.getScrollHeight() + " top=" + element.getScrollTop());
        if (element.getScrollTop() > element.getScrollHeight() - element.getOffsetHeight() - 400) {
            // 400px before scrolling hits bottom
            if (resultsTable.isVisible()) {
                resultsTable.fetchMore();
            }
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        searchBox.setFocus(true);
    }



    @Override
    protected void onDetach() {
        super.onDetach();
        for (HandlerRegistration reg : privateHandlers) {
            reg.removeHandler();
        }
        privateHandlers.clear();
    }

    @Override
    public String getQueryString() {
        return searchBox.getText();
    }

    @Override
    public void clearResults() {
        resultsTable.clear();
        resultsTable.setVisible(false);
        noResultsLabel.setVisible(false);
    }

    @Override
    public void setResults(RangeResponse<TrackDetail> results) {
        if (results.getTotalCount() > 0) {
            noResultsLabel.setVisible(false);
            resultsTable.setVisible(true);
            resultsTable.setData(results);
        } else {
            noResultsLabel.setVisible(true);
            resultsTable.setVisible(false);
            resultsTable.clear();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("clearButton")
    void onClearButtonClicked(ClickEvent e) {
        autoSearchTimer.cancel();
        searchBox.setText("");
        searchBox.setFocus(true);
        presenter.newSearchQuery(searchBox.getText());
    }

    @UiHandler("searchButton")
    void onSearchButtonClicked(ClickEvent e) {
        autoSearchTimer.cancel();
        presenter.newSearchQuery(searchBox.getText());
    }

    @UiHandler("searchBox")
    void onSearchBoxReturn(KeyDownEvent event) {
        /*
        * on "Return" key start query
        */
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            autoSearchTimer.cancel();
            presenter.newSearchQuery(searchBox.getText());
        } else {
            autoSearchTimer.cancel();
            autoSearchTimer.schedule(300);
        }
    }
}