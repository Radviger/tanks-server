package gtanks.system.quartz.impl;

import gtanks.system.quartz.QuartzJob;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

public enum QuartzServiceImpl implements QuartzService {
    INSTANCE;

    private Scheduler scheduler;

    QuartzServiceImpl() {
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();

        try {
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException ignored) { }
    }

    private JobDetail createJob(String name, String group, QuartzJob object) {
        JobDetail job = new JobDetail(name, group, QuartzJobRunner.class);
        job.getJobDataMap().put(QuartzJobRunner.jobRunKey, object);
        return job;
    }

    @Override
    public JobDetail addJobInterval(String name, String group, QuartzJob object, TimeType type, long interval, int repeatCount) {
        JobDetail job = this.createJob(name, group, object);

        try {
            SimpleTrigger trigger = new SimpleTrigger(name, group, repeatCount, type.time(interval));
            this.scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ignored) {}

        return job;
    }

    @Override
    public JobDetail addJobInterval(String name, String group, QuartzJob object, TimeType type, long interval) {
        return this.addJobInterval(name, group, object, type, interval, -1);
    }

    @Override
    public JobDetail addJob(String name, String group, QuartzJob object, TimeType type, long time) {
        JobDetail job = this.createJob(name, group, object);

        try {
            SimpleTrigger trigger = new SimpleTrigger(name, group, new Date(System.currentTimeMillis() + type.time(time)));
            this.scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ignored) {}

        return job;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public void deleteJob(String name, String group) {
        try {
            this.scheduler.deleteJob(name, group);
        } catch (SchedulerException ignored) { }
    }
}
