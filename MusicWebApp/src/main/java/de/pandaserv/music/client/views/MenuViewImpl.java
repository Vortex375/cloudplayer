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
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class MenuViewImpl extends Composite implements MenuView {
    @UiTemplate("MenuView.ui.xml")
    interface MenuViewUiBinder extends UiBinder<HTMLPanel, MenuViewImpl> {

    }
    private static MenuViewUiBinder ourUiBinder = GWT.create(MenuViewUiBinder.class);

    public MenuViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public void setPresenter(Presenter presenter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
