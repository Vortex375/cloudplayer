package de.pandaserv.music.server.cache;

import de.pandaserv.music.shared.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Magic File Input Stream that re-opens the file when EOF is hit
 */
public class TranscodeInputStream extends InputStream {
    static Logger logger = LoggerFactory.getLogger(TranscodeInputStream.class);

    private long id;
    private boolean magicEnabled;
    private InputStream actualInputStream;
    private long offset;

    public TranscodeInputStream(long id) {
        this.id = id;
        offset = 0;
        refreshFile();
    }

    private void refreshFile() {
        InputStream newInputStream;
        synchronized (CacheManager.getInstance()) {
            if (CacheManager.getInstance().getStatus(id) == FileStatus.TRANSCODING) {
                logger.info("refreshFile(): accessing partial file. >>magic refresh<<");
                magicEnabled = true;
                try {
                    newInputStream = CacheManager.getInstance().getTranscodeInputStream(id);
                } catch (FileNotFoundException e) {
                    logger.warn("refreshFile() failed: {}", e);
                    // can not refresh
                    return;
                }
            } else if (CacheManager.getInstance().getStatus(id) == FileStatus.PREPARED) {
                logger.info("refreshFile(): switching to completed file.");
                magicEnabled = false;
                try {
                    newInputStream = CacheManager.getInstance().getInputStream(id);
                } catch (IOException e) {
                    logger.warn("refreshFile() failed: {}", e);
                    // can not refresh
                    return;
                }
            } else {
                logger.warn("refreshFile() failed: file no longer available in cache O_o");
                // can not refresh
                return;
            }
        }
        if (actualInputStream != null) {
            try {
                actualInputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
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

    @Override
    public int read() throws IOException {
        int ret = actualInputStream.read();
        if (magicEnabled && ret < 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            refreshFile();
            return read();
        }
        offset += 1;
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = actualInputStream.read(b);
        if (magicEnabled && ret < 0) {
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
        if (magicEnabled && ret < 0) {
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
