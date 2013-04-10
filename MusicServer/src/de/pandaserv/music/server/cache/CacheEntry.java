package de.pandaserv.music.server.cache;

import de.pandaserv.music.shared.FileStatus;

/**
 *
 * @author ich
 */
class CacheEntry {
    private final long id;
    private long size; // file size
    private FileStatus status;
    private float completion;
    private String message;

    public CacheEntry(long id, FileStatus initialStatus) {
        this.id = id;
        this.status = initialStatus;
    }

    public CacheEntry(long id) {
        this.id = id;
        this.status = FileStatus.PREPARED;
    }

    public long getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public synchronized void setStatus(FileStatus status) {
        this.status = status;
    }
    
    public synchronized FileStatus getStatus() {
        return status;
    }

    public float getCompletion() {
        return completion;
    }

    public void setCompletion(float completion) {
        this.completion = completion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
