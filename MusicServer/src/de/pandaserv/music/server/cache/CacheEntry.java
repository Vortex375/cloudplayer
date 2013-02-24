package de.pandaserv.music.server.cache;

import de.pandaserv.music.server.devices.Device;
import de.pandaserv.music.shared.FileStatus;

import java.io.IOException;
import java.io.InputStream;

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

    private Device directAccessDevice;
    private String directAccessName;

    public CacheEntry(long id, FileStatus initialStatus) {
        this.id = id;
        this.status = initialStatus;
    }

    public CacheEntry(long id, Device directAccessDevice, String directAccessFileName) {
        this.id = id;
        this.directAccessDevice = directAccessDevice;
        this.directAccessName = directAccessFileName;
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

    public InputStream getDirectAccess() throws IOException {
        if (directAccessDevice != null) {
            return directAccessDevice.getFile(directAccessName);
        } else {
            return null;
        }
    }
}
