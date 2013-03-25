package de.pandaserv.music.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import de.pandaserv.music.client.misc.JSUtil;
import de.pandaserv.music.client.presenters.MusicTestPresenter;
import de.pandaserv.music.client.views.MusicTestViewImpl;
import de.pandaserv.music.shared.GwtMusicService;
import de.pandaserv.music.shared.GwtMusicServiceAsync;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MusicApp {

    private final long userId;

    public MusicApp(long userId) {
        this.userId = userId;
    }

    public void start() {

        MusicTestViewImpl view = new MusicTestViewImpl();
        MusicTestPresenter presenter = new MusicTestPresenter(view);

        view.setPresenter(presenter);
        Dictionary startupConfig = Dictionary.getDictionary("startupConfig");

        RootPanel.get().add(view);
    }
}
