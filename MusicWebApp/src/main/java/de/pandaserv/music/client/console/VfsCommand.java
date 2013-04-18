package de.pandaserv.music.client.console;

import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class VfsCommand implements Runnable {
    private Console console;
    private String[] args;

    public VfsCommand(Console console, String[] args) {
        this.console = console;
        this.args = args;
    }

    @Override
    public void run() {
        if (args.length != 1) {
            console.print("Missing parameter. Run 'help vfs' to see a list of available parameters.");
            console.runMainCommand();
            return;
        }

        if (args[0].equals("update")) {
            console.print("Updating vfs database...");
            console.showWait(true);
            RemoteService.getInstance().updateVfs(new MyAsyncCallback<Boolean>() {
                @Override
                protected void onResult(Boolean result) {
                    console.showWait(false);
                    if (result) {
                        console.print("success.");
                    } else {
                        console.print("There was an error while updating the database. Please see the server logs for details.");
                    }
                    console.runMainCommand();
                }
            });
        } else if (args[0].equals("rebuild")) {
            console.print("Rebuilding vfs database...");
            console.showWait(true);
            RemoteService.getInstance().rebuildVfs(new MyAsyncCallback<Boolean>() {
                @Override
                protected void onResult(Boolean result) {
                    console.showWait(false);
                    if (result) {
                        console.print("success.");
                    } else {
                        console.print("There was an error while rebuilding the database. Please see the server logs for details.");
                    }
                    console.runMainCommand();
                }
            });
        } else {
            console.print("Invalid parameter: " + args[0] + ". Use 'help vfs'.");
            console.runMainCommand();
        }
    }
}
