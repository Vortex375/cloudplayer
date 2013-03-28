package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PlaybackCurrentIdEventHandler extends EventHandler {
    public void onDuration(long id);
}
