package de.pandaserv.music.server.cache;

/**
 *
 * @author ich
 */
class CacheEntry {
    private final long id;
    private FileStatus status;
    private float completion;
    private String message;
    private PrepareJob prepareJob;

    public CacheEntry(long id, FileStatus initialStatus) {
        this.id = id;
        this.status = initialStatus;
    }

    public long getId() {
        return id;
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

    public PrepareJob getPrepareJob() {
        return prepareJob;
    }

    public void setPrepareJob(PrepareJob prepareJob) {
        this.prepareJob = prepareJob;
    }
}
