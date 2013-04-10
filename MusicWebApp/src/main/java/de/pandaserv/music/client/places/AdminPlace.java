package de.pandaserv.music.client.places;

public class AdminPlace extends AppPlace {

    public static final String ID = "admin";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public AdminPlace copy() {
        return new AdminPlace();
    }
}
