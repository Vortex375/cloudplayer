package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackCurrentIdEvent extends GwtEvent<PlaybackCurrentIdEventHandler> {

    public static final Type<PlaybackCurrentIdEventHandler> TYPE = new Type<PlaybackCurrentIdEventHandler>();

    private final long id;

    public PlaybackCurrentIdEvent(long id) {
        this.id = id;
    }

    @Override
    protected void dispatch(PlaybackCurrentIdEventHandler handler) {
        handler.onCurrentId(id);
    }

    @Override
    public Type<PlaybackCurrentIdEventHandler> getAssociatedType() {
        return TYPE;
    }
}
