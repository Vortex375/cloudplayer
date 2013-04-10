package de.pandaserv.music.client.console;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Console {
    public interface InputCallback {
        public void onInput(String input);
    }

    public void setMainCommand(Runnable main);
    public void runMainCommand();

    public void clear();
    public void print(String message);
    public void input(String message, final InputCallback callback);
}
