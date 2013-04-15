package de.pandaserv.music.server.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/13/13
 * Time: 9:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class CacheInputStream extends InputStream {
    private CacheEntry cacheEntry;
    private String transcodeCommand;
    private int seekSeconds;

    private PrepareJob prepareJob;
    private TranscodeJob transcodeJob;

    private InputStream actualInputStream;

    public CacheInputStream(CacheEntry cacheEntry, PrepareJob prepareJob, File entryFile, String transcodeCommand, int seekSeconds) throws IOException {
        this.cacheEntry = cacheEntry;
        this.transcodeCommand = transcodeCommand;
        this.seekSeconds = seekSeconds;
        this.prepareJob = prepareJob;

        CacheEntryInputStream inputStream = new CacheEntryInputStream(cacheEntry, entryFile);
        transcodeJob = new TranscodeJob(cacheEntry.getId(), inputStream, transcodeCommand, seekSeconds);

        actualInputStream = transcodeJob.setup();

        if (prepareJob != null) {
            new Thread(prepareJob).start();
        }
        new Thread(transcodeJob).start();
    }

    @Override
    public int read() throws IOException {
        return actualInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return actualInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return actualInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return actualInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return actualInputStream.available();
    }

    @Override
    public void close() throws IOException {
        transcodeJob.cancel();
        //TODO: this causes IOException to be thrown in the TranscodeJob's thread
        actualInputStream.close();
        if (prepareJob != null) {
            prepareJob.cancel();
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        actualInputStream.reset();
    }
}
