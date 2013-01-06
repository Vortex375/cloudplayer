package de.pandaserv.music.server.events;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 1/6/13
 * Time: 2:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class JobAddEvent {
    private long jobId;

    public JobAddEvent(long jobId) {
        this.jobId = jobId;
    }

    public long getJobId() {
        return jobId;
    }
}
