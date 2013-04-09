package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;
import de.pandaserv.music.shared.UserInfo;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackWaitingEvent extends GwtEvent<PlaybackWaitingEventHandler> {

    public static final Type<PlaybackWaitingEventHandler> TYPE = new Type<PlaybackWaitingEventHandler>();

    private final boolean waiting;

    public PlaybackWaitingEvent(boolean waiting) {
        this.waiting = waiting;
    }

    @Override
    protected void dispatch(PlaybackWaitingEventHandler handler) {
        handler.onPlaybackWaiting(waiting);
    }

    @Override
    public Type<PlaybackWaitingEventHandler> getAssociatedType() {
        return TYPE;
    }
}
