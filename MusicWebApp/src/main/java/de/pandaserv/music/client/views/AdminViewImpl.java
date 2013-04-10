package de.pandaserv.music.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import de.pandaserv.music.client.console.Console;
import de.pandaserv.music.client.widgets.HtmlConsole;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class AdminViewImpl extends Composite implements AdminView {
    @UiTemplate("AdminView.ui.xml")
    interface AdminViewUiBinder extends UiBinder<HTMLPanel, AdminViewImpl> {

    }
    private static AdminViewUiBinder ourUiBinder = GWT.create(AdminViewUiBinder.class);

    @UiField
    HtmlConsole console;

    public AdminViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public Console getConsole() {
        return console;
    }

    @Override
    public void setPresenter(Presenter presenter) {

    }
}