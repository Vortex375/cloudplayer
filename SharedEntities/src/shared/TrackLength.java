package de.pandaserv.music.shared;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/12/13
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrackLength implements Serializable {
    private long id;
    private int duration;
    private long fileSize;

    public TrackLength() {
    }

    public TrackLength(long id, int duration, long fileSize) {
        this.id = id;
        this.duration = duration;
        this.fileSize = fileSize;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
