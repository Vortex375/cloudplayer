package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;
import de.pandaserv.music.client.misc.PlaybackStatus;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackStatusEvent extends GwtEvent<PlaybackStatusEventHandler> {

    public static final Type<PlaybackStatusEventHandler> TYPE = new Type<PlaybackStatusEventHandler>();

    private final PlaybackStatus status;

    public PlaybackStatusEvent(PlaybackStatus status) {
        super();
        this.status = status;
    }

    @Override
    protected void dispatch(PlaybackStatusEventHandler handler) {
        handler.onPlaybackStatus(status);
    }

    @Override
    public Type<PlaybackStatusEventHandler> getAssociatedType() {
        return TYPE;
    }
}
