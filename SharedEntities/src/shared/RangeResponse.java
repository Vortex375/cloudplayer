package de.pandaserv.music.shared;

import com.google.gwt.view.client.Range;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/8/13
 * Time: 6:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RangeResponse<T extends DataType> implements Serializable {
    private long queryId;

    private Range range;
    private int totalCount;

    private T[] data;

    public RangeResponse() {
    }

    public RangeResponse(long queryId, T[] data, Range range, int totalCount) {
        this.data = data;
        this.queryId = queryId;
        this.range = range;
        this.totalCount = totalCount;
    }

    public T[] getData() {
        return data;
    }

    public void setData(T[] data) {
        this.data = data;
    }

    public long getQueryId() {
        return queryId;
    }

    public void setQueryId(long queryId) {
        this.queryId = queryId;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
