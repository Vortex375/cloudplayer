package de.pandaserv.music.server.jobs;

public interface Job extends Runnable {
    public String getDescription();
    public float getCompletion();
    public void cancel();
}
