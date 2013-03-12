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
public class MusicTest implements EntryPoint {

    //TODO: testing only
    private static final GwtMusicServiceAsync service = GWT.create(GwtMusicService.class);

    public static GwtMusicServiceAsync getService() {
        return service;
    }

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

          @Override
          public void onUncaughtException(Throwable e) {
              JSUtil.log("uncaught: " + e.getMessage());
              e.printStackTrace();
          }
      });

      // set up remote service
      ((ServiceDefTarget) service).setServiceEntryPoint("/service/gwt");

      MusicTestViewImpl view = new MusicTestViewImpl();
      MusicTestPresenter presenter = new MusicTestPresenter(view);

      view.setPresenter(presenter);
      Dictionary startupConfig = Dictionary.getDictionary("startupConfig");
      //presenter.setStreamId(Integer.parseInt(startupConfig.get("demoStreamId")));

      service.testLogin(new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable throwable) {
              //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override
          public void onSuccess(Void aVoid) {
              //To change body of implemented methods use File | Settings | File Templates.
          }
      });

      RootPanel.get().add(view);
  }
}
