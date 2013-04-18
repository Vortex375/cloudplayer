package de.pandaserv.music.client.console;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainCommand implements Runnable {
    private Console console;

    public MainCommand(Console console) {
        this.console = console;

        printWelcomeMessage();
    }

    @Override
    public void run() {
        console.input(">", new Console.InputCallback() {
            @Override
            public void onInput(String input) {
                input(input);
            }
        });
    }

    private void input(String input) {
        String[] split = input.split(" ");
        for (int i = 0; i < split.length; i++) {
            // remove excess white-space
            split[i] = split[i].trim().replace("\u200B", ""); // replace zero width space character
        }

        if (split.length < 1) {
            // no command
            run();
            return;
        }
        String command = split[0];
        String[] args;
        if (split.length > 1) {
            args = new String[split.length-1];
            System.arraycopy(split, 1, args, 0, args.length);
        } else {
            args = new String[0];
        }

        if (command.equals("clear")) {
            console.clear();
            printWelcomeMessage();
            run();
        } else if (command.equals("help")) {
            printHelp(args);
            run();
        } else if (command.equals("user")) {
            new UserCommand(console, args).run();
        } else if (command.equals("device")) {
            new DeviceCommand(console, args).run();
        } else if (command.equals("vfs")) {
            new VfsCommand(console, args).run();
        } else {
            console.print("Unknown command: " + command + ". Type 'help' for a list of available commands.");
            run();
        }
    }

    private void printWelcomeMessage() {
        console.print("Welcome to the PandaServ Music Administration Console!");
        console.print("");
        console.print("Type 'help' for a list of available commands.");
        console.print("");
    }

    private void printHelp(String[] args) {
        if (args.length == 1) {
            if (args[0].equals("user")) {
                console.print("");
                console.print("Help for command 'user': ");
                console.print("");
                console.print("user add <username>: create a new user account with name <username>." +
                        " You will be prompted for a password.");
                console.print("user delete <username>: delete the user account with given username." +
                        " You can not delete your own account.");
                console.print("user passwd <username>: change the password of the user account with given username." +
                        " You will be prompted for the new password.");
            } else if (args[0].equals("device")) {
                console.print("");
                console.print("Help for command 'device':");
                console.print("");
                console.print("device list: list all devices and their properties.");
                console.print("device add <name> <type>: add a new device.");
                console.print("device delete <name>: delete a device." +
                        "Note that this will also remove all tracks from this devices from the database.");
                console.print("device set <name> <property> <value>: set a property value for a device.");
                console.print("device unset <name> <property>: unset a property for a device.");
            } else if (args[0].equals("vfs")) {
                console.print("");
                console.print("Help for command 'vfs':");
                console.print("");
                console.print("vfs update: update the virtual filesystem database (e.g. after adding or removing tracks)");
                console.print("vfs rebuild: delete and rebuild the entire virtual filesystem database");
            } else {
                console.print("No help available for '" + args[0] + "'. Type 'help' for a list of available commands.");
            }
        } else {
            console.print("");
            console.print("Known commands:");
            console.print("");
            console.print("'user': modify user accounts.");
            console.print("'device': modify devices (media sources).");
            console.print("'vfs': update or rebuild the virtual filesystem database");
            console.print("");
            console.print("Type 'help <command>' to view detailed options for each command.");
        }
    }
}
