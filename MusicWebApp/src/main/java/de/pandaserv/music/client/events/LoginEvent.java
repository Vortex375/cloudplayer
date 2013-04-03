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
public class LoginEvent extends GwtEvent<LoginEventHandler> {

    public static final Type<LoginEventHandler> TYPE = new Type<LoginEventHandler>();

    private final UserInfo user;

    public LoginEvent(UserInfo user) {
        this.user = user;
    }

    @Override
    protected void dispatch(LoginEventHandler handler) {
        handler.onLogin(user);
    }

    @Override
    public Type<LoginEventHandler> getAssociatedType() {
        return TYPE;
    }
}
