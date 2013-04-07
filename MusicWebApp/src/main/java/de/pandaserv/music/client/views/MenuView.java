package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/5/13
 * Time: 10:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MenuView extends IsWidget {
    public interface Presenter {

    }

    void setPresenter(Presenter presenter);
}
