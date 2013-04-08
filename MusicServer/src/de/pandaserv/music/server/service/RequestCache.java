package de.pandaserv.music.server.service;

import de.pandaserv.music.server.cache.CacheManager;
import de.pandaserv.music.shared.DataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/8/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestCache {
    // Singleton
    private static RequestCache ourInstance;
    public static RequestCache getInstance() {
        if (ourInstance == null) {
            ourInstance = new RequestCache();
        }
        return ourInstance;
    }

    private Map<Long, DataType[]> cache;

    private RequestCache() {
        cache = new HashMap<>();
    }

    public synchronized void drop(long queryId) {
        if (cache.containsKey(queryId)) {
            cache.remove(queryId);
        }
    }

    public synchronized void put(long queryId, DataType[] data) {
        cache.put(queryId, data);
    }

    public synchronized DataType[] get(long queryId) {
        if (cache.containsKey(queryId)) {
            return cache.get(queryId);
        } else {
            return null;
        }
    }
}
