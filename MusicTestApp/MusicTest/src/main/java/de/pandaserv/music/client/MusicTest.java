package de.pandaserv.music.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import de.pandaserv.music.client.presenters.MusicTestPresenter;
import de.pandaserv.music.client.views.MusicTestView;
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

      RootPanel.get().add(view);
  }
}
