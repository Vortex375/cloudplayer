package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class VisDataEvent extends GwtEvent<VisDataEventHandler> {

    public static final Type<VisDataEventHandler> TYPE = new Type<VisDataEventHandler>();

    private final int[] data;

    public VisDataEvent(int[] data) {
        super();
        this.data = data;
    }

    @Override
    protected void dispatch(VisDataEventHandler handler) {
        handler.onVisData(data);
    }

    @Override
    public Type<VisDataEventHandler> getAssociatedType() {
        return TYPE;
    }
}
