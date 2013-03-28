package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.EventHandler;
import de.pandaserv.music.client.misc.PlaybackStatus;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PlaybackStatusEventHandler extends EventHandler {
    public void onPlaybackStatus(PlaybackStatus status);
}
