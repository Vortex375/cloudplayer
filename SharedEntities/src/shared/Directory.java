package de.pandaserv.music.shared;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/16/13
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Directory implements Serializable, DataType {
    private long nodeId;
    private long parent;
    private String device;
    private String path;
    private long trackId;

    public Directory() {
    }

    // constructor for directories
    public Directory(long nodeId, String device, long parent, String path) {
        this.device = device;
        this.nodeId = nodeId;
        this.parent = parent;
        this.path = path;
        this.trackId = -1;
    }

    // constructor for files
    public Directory(long nodeId, String device, long parent, String path, long trackId) {
        this.device = device;
        this.nodeId = nodeId;
        this.parent = parent;
        this.path = path;
        this.trackId = trackId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }
}
