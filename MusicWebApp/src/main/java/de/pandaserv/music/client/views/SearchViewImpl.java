package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.HandlerRegistration;
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

    private Timer resizeTimer;

    private List<HandlerRegistration> privateHandlers;

    public SearchViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        autoSearchTimer = new Timer() {
            @Override
            public void run() {
                presenter.newSearchQuery(searchBox.getText());
            }
        };

        resizeTimer = new Timer() {
            @Override
            public void run() {
                resizeResultsTable();
            }
        };

        privateHandlers = new ArrayList<HandlerRegistration>();

        resultsTable.addTrackClickHandler(new SearchResultsTable.TrackClickHandler() {
            @Override
            public void onTrackClicked(long trackId) {
                presenter.onTrackClicked(trackId);
            }
        });

        //resizeTimer.schedule(1);
    }

    private void resizeResultsTable() {
        /*
         * manually resize table so it doesn't overflow
         *
         * TODO: currently all columns are set to equal sizes
         */
        int columnWidth = getOffsetWidth() / resultsTable.getColumnCount();

        for (int i = 0; i < resultsTable.getColumnCount(); i++) {
            resultsTable.setColumnWidth(i, columnWidth + "px");
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        // fetch more rows when the user scrolls to the bottom of the page
        GWT.log("add window scroll handler");
        privateHandlers.add(Window.addWindowScrollHandler(new Window.ScrollHandler() {
            @Override
            public void onWindowScroll(Window.ScrollEvent scrollEvent) {
                GWT.log("window scroll");
            }
        }));
        /*privateHandlers.add(Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                resizeTimer.schedule(1);
            }
        }));*/

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