package de.pandaserv.music.client.remote;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/25/13
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MyAsyncCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable caught) {
        //TODO
        Window.alert("Call failed: " + caught.getMessage());
    }

    @Override
    public void onSuccess(T result) {
        //TODO: caching etc.
        onResult(result);
    }

    protected abstract void onResult(T result);
}
