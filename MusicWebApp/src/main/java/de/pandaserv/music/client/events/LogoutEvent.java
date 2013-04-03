package de.pandaserv.music.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogoutEvent extends GwtEvent<LogoutEventHandler> {

    public static final Type<LogoutEventHandler> TYPE = new Type<LogoutEventHandler>();

    @Override
    protected void dispatch(LogoutEventHandler handler) {
        handler.onLogout();
    }

    @Override
    public Type<LogoutEventHandler> getAssociatedType() {
        return TYPE;
    }
}
