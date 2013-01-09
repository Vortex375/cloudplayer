package de.pandaserv.music.server.jobs;

import com.adamtaft.eb.EventBusService;
import de.pandaserv.music.server.devices.DeviceManager;
import de.pandaserv.music.server.events.JobAddEvent;
import de.pandaserv.music.server.events.JobRemoveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 1/6/13
 * Time: 2:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class JobManager {
    static Logger logger = LoggerFactory.getLogger(JobManager.class);

    private Map<Long, Job> jobs;
    private long jobId;

    // Singleton
    private static JobManager ourInstance;

    public static JobManager getInstance() {
        return ourInstance;
    }

    public static JobManager setup() {
        if (ourInstance != null) {
            logger.warn("JobManager.setup() called but there is already an instance!");
        } else {
            ourInstance = new JobManager();
        }

        return ourInstance;
    }

    private JobManager() {
        jobs = new HashMap<>();
        jobId = 0;
    }

    public synchronized long addJob(Job job) {
        jobs.put(jobId++, job);

        EventBusService.publish(new JobAddEvent(jobId));

        return jobId-1;
    }

    public synchronized void removeJob(long id) {
        if (jobs.containsKey(id)) {
            jobs.remove(id);
            EventBusService.publish(new JobRemoveEvent(id));
        }
    }

    public synchronized Map<Long, Job> listJobs() {
        // return a copy of the job map

        Map<Long,Job> ret = new HashMap<>();
        for (long key: jobs.keySet()) {
            ret.put(key, jobs.get(key));
        }

        return ret;
    }

    public synchronized Job getJob(long id) {
        if (jobs.containsKey(id)) {
            return jobs.get(id);
        } else {
            return null;
        }
    }
}
