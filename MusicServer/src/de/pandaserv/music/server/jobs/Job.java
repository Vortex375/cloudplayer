package de.pandaserv.music.server.jobs;

public interface Job extends Runnable {
    public String getDescription();
    public String getStatus();
    public void cancel();
}
