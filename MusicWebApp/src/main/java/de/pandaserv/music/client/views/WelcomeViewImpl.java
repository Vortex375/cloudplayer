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
public class WelcomeViewImpl extends Composite implements WelcomeView {
    @UiTemplate("WelcomeView.ui.xml")
    interface WelcomeViewUiBinder extends UiBinder<HTMLPanel, WelcomeViewImpl> {

    }
    private static WelcomeViewUiBinder ourUiBinder = GWT.create(WelcomeViewUiBinder.class);

    public WelcomeViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public void setPresenter(Presenter presenter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}