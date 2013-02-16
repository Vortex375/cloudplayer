package de.pandaserv.music.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.RootPanel;
import de.pandaserv.music.client.presenters.MusicTestPresenter;
import de.pandaserv.music.client.views.MusicTestViewImpl;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MusicTest implements EntryPoint {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      MusicTestViewImpl view = new MusicTestViewImpl();
      MusicTestPresenter presenter = new MusicTestPresenter(view);

      view.setPresenter(presenter);
      Dictionary startupConfig = Dictionary.getDictionary("startupConfig");
      presenter.setStreamId(Integer.parseInt(startupConfig.get("demoStreamId")));

      RootPanel.get().add(view);
  }
}
