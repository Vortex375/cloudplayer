package de.pandaserv.music.server.cache;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/26/13
 * Time: 10:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class TranscodeInputStream_ extends InputStream {
    private static final int INITIAL_BUFFER_SIZE = 5242880; // 5MB
    private static final int BUFFER_EXTEND_STEP = 1048576; // 1MB

    private byte[] data;
    private int writePos;
    private ThreadLocal<Integer> readPos; // allow reads from multiple threads
    private boolean finished; // no more data is to be added
    private boolean errored;

    public TranscodeInputStream_() {
        data = new byte[INITIAL_BUFFER_SIZE];
        readPos = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        writePos = 0;
        finished = false;
        errored = false;
    }

    @Override
    public synchronized int read() throws IOException {
        while (!finished && readPos.get() >= writePos) {
            try {
                wait(); // block until data is available or end of stream is reached
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (readPos.get() >= writePos) {
            // no more data can be read - end of stream reached
            return -1;
        } else {
            int ret = data[readPos.get()];
            readPos.set(readPos.get()  + 1);
            return ret;
        }
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
        while (!finished && readPos.get() >= writePos) {
            try {
                wait(); // block until data is available or end of stream is reached
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (readPos.get() >= writePos) {
            // no more data can be read - end of stream reached
            return -1;
        } else {
            int len = Math.min(writePos - readPos.get(), b.length);
            System.arraycopy(data, readPos.get(), b, 0, len);
            readPos.set(readPos.get() + len);
            return len;
        }
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        while (!finished && readPos.get() >= writePos) {
            try {
                wait(); // block until data is available or end of stream is reached
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (readPos.get() >= writePos) {
            // no more data can be read - end of stream reached
            return -1;
        } else {
            len = Math.min(writePos - readPos.get(), len);
            System.arraycopy(data, readPos.get(), b, off, len);
            readPos.set(readPos.get() + len);
            return len;
        }
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        // read pos is expressed as integer - we can therefore only skip a number of bytes
        // that fits in an integer
        if (n > Integer.MAX_VALUE || n < Integer.MIN_VALUE) {
            throw new IOException("Unable to skip: parameter out of range");
        }
        readPos.set(readPos.get() + (int) n);
        return n;
    }

    @Override
    public synchronized int available() throws IOException {
        int ret = writePos - readPos.get();
        if (ret < 0) // shouldn't happen
            return 0;
        return ret;
    }

    public synchronized void pushData(byte[] b, int off, int len) {
        if (writePos + len > data.length) {
            // expand array
            byte[] dataNew = new byte[data.length + len + BUFFER_EXTEND_STEP];
            System.arraycopy(data, 0, dataNew, 0, writePos);
            data = dataNew;
            // hopefully the old data array is garbage collected
        }
        System.arraycopy(b, off, data, writePos, len);
        writePos += len;
        notifyAll();
    }

    public synchronized void finishWrite() {
        if (finished) {
            return;
        }
        // truncate array
        byte[] dataNew = new byte[writePos];
        System.arraycopy(data, 0, dataNew, 0, writePos);
        data = dataNew;
        finished = true;
        notifyAll();
    }

    public boolean isErrored() {
        return errored;
    }

    public void setErrored(boolean errored) {
        this.errored = errored;
    }

    public synchronized void rewind() {
        readPos.set(0);
    }

    public byte[] getData() {
        return data;
    }
}
