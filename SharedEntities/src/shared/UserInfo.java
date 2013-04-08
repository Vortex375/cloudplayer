package de.pandaserv.music.shared;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/3/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserInfo implements Serializable, DataType {
    private long id;
    private String name;

    public UserInfo() {
    }

    public UserInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
