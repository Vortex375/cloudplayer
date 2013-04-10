package de.pandaserv.music.client.activities;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import de.pandaserv.music.client.console.MainCommand;
import de.pandaserv.music.client.views.AdminView;
import de.pandaserv.music.client.views.AdminViewImpl;

public class AdminActivity extends MyActivity implements CacheableActivity {
    private AdminView view;

    public AdminActivity() {
        view = new AdminViewImpl();

        view.getConsole().setMainCommand(new MainCommand(view.getConsole()));
        view.getConsole().runMainCommand();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
    }
}
