package de.pandaserv.music.client.i18n;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/25/13
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
@LocalizableResource.GenerateKeys("com.google.gwt.i18n.server.keygen.MD5KeyGenerator")
@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat", fileName = "Messages")
public interface GuiConstants extends Constants {
    @Description("username")
    @DefaultStringValue("Username")
    public String username();

    @Description("password")
    @DefaultStringValue("Password")
    public String password();

    @Description("login")
    @DefaultStringValue("Login")
    public String login();

    @Description("pleaseWait")
    @DefaultStringValue("Please wait...")
    public String pleaseWait();
}
