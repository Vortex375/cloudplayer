package de.pandaserv.music.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Track Data Object
 *
 * Contains all information on a track in the database
 */
public class Track implements Serializable {
    private long id;
    private String device;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private int track;
    private int year;
    private String devicePath;
    private String cover;
    private Date lastModified;

    public Track() {

    }

    public Track(long id, String device, String title, String artist, String album, String genre, int track, int year, String devicePath, Date lastModified) {
        this.id = id;
        this.device = device;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.track = track;
        this.year = year;
        this.devicePath = devicePath;
        this.lastModified = lastModified;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
