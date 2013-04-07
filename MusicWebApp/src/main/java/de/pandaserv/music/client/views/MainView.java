package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface MainView extends IsWidget {
    public interface Presenter {
        void onLogoutButtonClicked();
        void onMessagesClicked();
    }

    AcceptsOneWidget getMenuColumn();
    AcceptsOneWidget getMainColumn();
    //HasWidgets getPlaylistColumn();

    void setUsername(String username);

    void setPresenter(Presenter presenter);
}
