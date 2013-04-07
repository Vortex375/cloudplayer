package de.pandaserv.music.client.places;

public class FailPlace extends AppPlace {

    public static final String ID = "fail";

    private final String token;

    public FailPlace(String token) {
        this.token = token;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public FailPlace copy() {
        return new FailPlace(token);
    }

    @Override
    public String toToken() {
        // fail place has no own token
        return token;
    }
}
