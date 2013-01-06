package de.pandaserv.music.server.events;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 1/6/13
 * Time: 2:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareCompleteEvent {
    private final long id;

    public PrepareCompleteEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
