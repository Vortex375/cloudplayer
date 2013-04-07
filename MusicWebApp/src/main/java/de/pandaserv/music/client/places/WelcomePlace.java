package de.pandaserv.music.client.places;

public class WelcomePlace extends AppPlace {

    public static final String ID = "home";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public WelcomePlace copy() {
        return new WelcomePlace();
    }
}
