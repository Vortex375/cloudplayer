package de.pandaserv.music.server.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Magic File Input Stream that re-opens the file when EOF is hit
 */
public class CacheEntryInputStream extends InputStream {
    static Logger logger = LoggerFactory.getLogger(CacheEntryInputStream.class);

    private final CacheEntry cacheEntry;
    private final File entryFile;
    private boolean magicEnabled;
    private InputStream actualInputStream;
    private long offset;
    private long available;

    public CacheEntryInputStream(CacheEntry cacheEntry, File entryFile) {
        this.cacheEntry = cacheEntry;
        this.entryFile = entryFile;
        offset = 0;
        refreshFile();
    }

    private void refreshFile() {
        InputStream newInputStream;
        synchronized (cacheEntry) {
            if (cacheEntry.getAvailable() < cacheEntry.getFileSize()) {
                available = cacheEntry.getAvailable();
                logger.info("refreshFile(): accessing partial file. >>magic refresh<<");
                magicEnabled = true;
            } else {
                logger.info("refreshFile(): switching to completed file.");
                magicEnabled = false;
            }
            try {
                newInputStream = new FileInputStream(entryFile);
            } catch (FileNotFoundException e) {
                logger.warn("refreshFile() failed: {}", e);
                // can not refresh
                return;
            }

            /*
             * close the old input stream
             */
            if (actualInputStream != null) {
                try {
                    actualInputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            /*
             * seek to correct offset
             */
            try {
                long skipped = newInputStream.skip(offset);
                if (skipped != offset) {
                    logger.warn("refreshFile() failed: can not seek to correct offset");
                    // can not refresh
                    return;
                }
            } catch (IOException e) {
                logger.warn("refreshFile() failed: {}", e);
                // can not refresh
                return;
            }
            actualInputStream = newInputStream;
        }
    }

    @Override
    public int read() throws IOException {
        if (magicEnabled && offset >= available) { // offset should actually never be > available
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            refreshFile();
            return read();
        }
        int ret = actualInputStream.read();
        offset += 1;
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = actualInputStream.read(b);
        if (magicEnabled && (offset + ret) > available) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            refreshFile();
            return read(b);
        }
        offset += ret;
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = actualInputStream.read(b, off, len);
        if (magicEnabled && (offset + ret) > available) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            refreshFile();
            return read(b, off, len);
        }
        offset += ret;
        return ret;
    }

    @Override
    public long skip(long n) throws IOException {
        long ret = actualInputStream.skip(n);
        offset += ret;
        return ret;
    }

    @Override
    public void close() throws IOException {
        actualInputStream.close();
    }

    @Override
    public int available() throws IOException {
        return actualInputStream.available();
    }

    @Override
    public synchronized void reset() throws IOException {
        actualInputStream.reset();
    }
}
