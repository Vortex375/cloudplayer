package de.pandaserv.music.shared;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/17/13
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccessDeniedException extends Exception {
    public AccessDeniedException() {
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
