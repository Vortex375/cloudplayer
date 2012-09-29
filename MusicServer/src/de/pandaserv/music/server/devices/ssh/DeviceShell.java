/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.devices.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Fake "shell" for ssh connections that does not allow to run any commands
 * and only listens for Ctrl-C and Ctrl-D to close the connection.
 *
 * @author ich
 */
public class DeviceShell implements Command {

    private ExitCallback exitCallback;

    private class InputThread extends Thread {

        private final InputStream in;

        public InputThread(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            InputStreamReader reader = new InputStreamReader(in);
            int input;
            while (true) {
                try {
                    input = reader.read();
                    // watch for control+C and control+D
                    if (input == 3 || input == 4) {
                        exitCallback.onExit(0);
                        break;
                    }
                } catch (IOException ex) {
                    if (exitCallback != null) {
                        exitCallback.onExit(1);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setInputStream(InputStream in) {
        InputThread inputThread = new InputThread(in);
        inputThread.start();
    }

    @Override
    public void setOutputStream(OutputStream out) {
    }

    @Override
    public void setErrorStream(OutputStream out) {
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        exitCallback = ec;
    }

    @Override
    public void start(Environment e) throws IOException {
    }

    @Override
    public void destroy() {
    }
}
