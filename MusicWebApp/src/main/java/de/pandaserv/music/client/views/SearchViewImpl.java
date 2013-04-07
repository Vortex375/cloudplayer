package de.pandaserv.music.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

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

    public SearchViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}