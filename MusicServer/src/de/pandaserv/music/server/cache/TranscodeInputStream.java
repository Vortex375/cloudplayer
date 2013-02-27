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
public class TranscodeInputStream extends InputStream {
    @Override
    public synchronized int read() throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
        return super.read(b);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        return super.skip(n);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public synchronized int available() throws IOException {
        return super.available();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
