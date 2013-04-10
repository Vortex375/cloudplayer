package de.pandaserv.music.client.console;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserCommand implements Runnable {
    private Console console;

    public UserCommand(Console console, String[] args) {

    }

    @Override
    public void run() {
        console.runMainCommand();
    }
}
