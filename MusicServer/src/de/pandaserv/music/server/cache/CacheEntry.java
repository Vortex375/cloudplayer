package de.pandaserv.music.server.cache;

import de.pandaserv.music.shared.FileStatus;

/**
 *
 * @author ich
 */
class CacheEntry {
    private final long id;
    private long fileSize; // total file size
    private int duration; // duration in seconds
    private long available; // how many bytes are currently in the cache
    private FileStatus status;
    private String message;

    private final Object writeLock = new Object(); // lock write access

    public CacheEntry(long id, FileStatus initialStatus) {
        this.id = id;
        this.status = initialStatus;
    }

    public CacheEntry(long id) {
        this(id, FileStatus.PREPARED);
    }

    public long getId() {
        return id;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public synchronized long getAvailable() {
        return available;
    }

    public synchronized void setAvailable(long available) {
        this.available = available;
    }

    public synchronized void setStatus(FileStatus status) {
        this.status = status;
    }
    
    public synchronized FileStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getWriteLock() {
        return writeLock;
    }
}
