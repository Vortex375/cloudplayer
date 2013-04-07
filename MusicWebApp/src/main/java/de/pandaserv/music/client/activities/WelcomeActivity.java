package de.pandaserv.music.client.activities;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import de.pandaserv.music.client.presenters.WelcomePresenter;
import de.pandaserv.music.client.views.WelcomeView;
import de.pandaserv.music.client.views.WelcomeViewImpl;

public class WelcomeActivity extends MyActivity {
    private WelcomeView view;
    private WelcomePresenter presenter;

    public WelcomeActivity() {
        view = new WelcomeViewImpl();
        presenter = new WelcomePresenter(view);
        view.setPresenter(presenter);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
    }
}
