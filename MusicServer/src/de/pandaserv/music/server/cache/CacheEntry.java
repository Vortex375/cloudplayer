package de.pandaserv.music.server.cache;

/**
 *
 * @author ich
 */
class CacheEntry {
    private final int id;
    private FileStatus status;
    private PrepareJob prepareJob;

    public CacheEntry(int id, String localPath, FileStatus initialStatus) {
        this.id = id;
        this.status = initialStatus;
    }

    public int getId() {
        return id;
    }
    
    public synchronized void setStatus(FileStatus status) {
        this.status = status;
    }
    
    public synchronized FileStatus getStatus() {
        return status;
    }

    public synchronized void setPrepareJob(PrepareJob prepareJob) {
        this.prepareJob = prepareJob;
    }
    
    public synchronized PrepareJob getPrepareJob() {
        return prepareJob;
    }
    
    
}
