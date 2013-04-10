package de.pandaserv.music.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import de.pandaserv.music.client.console.Console;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AdminView extends IsWidget {
    public interface Presenter {

    }

    Console getConsole();

    void setPresenter(Presenter presenter);
}
