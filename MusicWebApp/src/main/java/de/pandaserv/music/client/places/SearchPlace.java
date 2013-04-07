package de.pandaserv.music.client.places;

public class SearchPlace extends AppPlace {

    public static final String ID = "search";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public SearchPlace copy() {
        return new SearchPlace();
    }
}
