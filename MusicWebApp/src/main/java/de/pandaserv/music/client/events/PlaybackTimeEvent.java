package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackTimeEvent extends GwtEvent<PlaybackTimeEventHandler> {

    public static final Type<PlaybackTimeEventHandler> TYPE = new Type<PlaybackTimeEventHandler>();

    private final double duration;

    public PlaybackTimeEvent(double duration) {
        this.duration = duration;
    }

    @Override
    protected void dispatch(PlaybackTimeEventHandler handler) {
        handler.onDuration(duration);
    }

    @Override
    public Type<PlaybackTimeEventHandler> getAssociatedType() {
        return TYPE;
    }
}
