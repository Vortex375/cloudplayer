/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.server.ssh;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.*;

/**
 * Fake "shell" for ssh connections that does not allow to run any commands
 * and only listens for Ctrl-C and Ctrl-D to close the connection.
 *
 * @author ich
 */
public class DeviceShell implements Command {

    private ExitCallback exitCallback;
    private InputThread inputThread;
    private OutputThread outputThread;

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

    private class OutputThread extends Thread {
        private final OutputStream out;
        private boolean running;

        private OutputThread(OutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            running = true;
            OutputStreamWriter writer = new OutputStreamWriter(out);
            while(running) {
                try {
                    writer.write("keep alive\n");
                    Thread.sleep(60000);
                    writer.flush();
                } catch (IOException e) {
                    // stop running on error
                    return;
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        public synchronized void abort() {
            running = false;
        }
    }

    @Override
    public void setInputStream(InputStream in) {
        inputThread = new InputThread(in);
        inputThread.start();
    }

    @Override
    public void setOutputStream(OutputStream out) {
        // send welcome message
        PrintWriter writer = new PrintWriter(out);
        writer.print("You have successfully connected to Music Server.\r\n");
        writer.print("\r\n");
        writer.print("Press Control-C or Control-D to close this connection.\r\n");
        writer.flush();
        //writer.close();
        outputThread = new OutputThread(out);
        outputThread.start();
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
        outputThread.abort();
    }
}
