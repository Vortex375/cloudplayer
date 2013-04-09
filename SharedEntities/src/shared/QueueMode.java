package de.pandaserv.music.shared;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueueMode implements Serializable, DataType {
    public static class Other extends QueueMode {

    }

    public static class Playlist extends QueueMode {
        private String playlist;

        public Playlist() {
        }

        public Playlist(String playlist) {
            this.playlist = playlist;
        }

        public String getPlaylist() {
            return playlist;
        }

        public void setPlaylist(String playlist) {
            this.playlist = playlist;
        }
    }

    public static class AlbumOf extends QueueMode {
        private long id;

        public AlbumOf() {
        }

        public AlbumOf(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    public static class Query extends QueueMode {
        private String query; //TODO: string?

        public Query() {
        }

        public Query(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
