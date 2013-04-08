package de.pandaserv.music.shared;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/6/13
 * Time: 6:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cover implements DataType {
    private String md5;
    private byte[] data;
    private String mimeType;

    public Cover(byte[] data, String md5, String mimeType) {
        this.data = data;
        this.md5 = md5;
        this.mimeType = mimeType;
    }

    public Cover() {
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
