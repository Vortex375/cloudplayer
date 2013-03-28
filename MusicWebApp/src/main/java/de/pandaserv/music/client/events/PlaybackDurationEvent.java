package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackDurationEvent extends GwtEvent<PlaybackDurationEventHandler> {

    public static final Type<PlaybackDurationEventHandler> TYPE = new Type<PlaybackDurationEventHandler>();

    private final double time;

    public PlaybackDurationEvent(double time) {
        this.time = time;
    }

    @Override
    protected void dispatch(PlaybackDurationEventHandler handler) {
        handler.onDuration(time);
    }

    @Override
    public Type<PlaybackDurationEventHandler> getAssociatedType() {
        return TYPE;
    }
}
