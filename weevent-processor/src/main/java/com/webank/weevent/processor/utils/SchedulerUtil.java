package com.webank.weevent.processor.utils;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import java.text.ParseException;
import com.webank.weevent.processor.config.AutoWiringSpringBeanJobFactory;
import com.webank.weevent.processor.model.JobConfig;

@Slf4j
public class SchedulerUtil {

    private static StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    private static CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
    private static JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    private static AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
    private static SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

    static {
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("processor.properties"));
    }

    /**
     * create scheduler
     *
     * @param config job entity
     * @param context application context
     * @return boolean true or false
     */
    public static boolean createScheduler(JobConfig config, ApplicationContext context) {
        try {
            //create scheduler
            return create(config, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete old scheduler and new scheduler
     * @param oldConfig  old job
     * @param config  job entity
     * @param context application context
     * @return  boolean true or false
     */
    public static Boolean modifyScheduler(JobConfig oldConfig, JobConfig config, ApplicationContext context) {
        if (oldConfig == null || config == null || context == null) {
            return false;
        }
        try {
            String oldJobClassStr = oldConfig.getFullEntity();
            String oldName = oldJobClassStr + oldConfig.getId();
            String oldGroupName = oldConfig.getGroupName();
            //clear old scheduler
            delete(oldName, oldGroupName);
            //new scheduler
            return create(config, context);
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get the delete schedule  of function
     * @param oldName  old job name
     * @param oldGroupName old job group name
     * @return boolean
     * @throws SchedulerException
     */
    private static Boolean delete(String oldName, String oldGroupName) throws SchedulerException {
        TriggerKey key = new TriggerKey(oldName, oldGroupName);
        Scheduler oldScheduler = schedulerFactory.getScheduler();

        Trigger keyTrigger = oldScheduler.getTrigger(key);
        if (keyTrigger != null) {
            oldScheduler.unscheduleJob(key);
        }
        return true;
    }

    /**
     * get the new scheduler function
     * @param config job entity
     * @param context application context
     * @return boolean
     */
    private static Boolean create(JobConfig config, ApplicationContext context) {
        try {
            //create
            String jobClassStr = config.getFullEntity();
            Class clazz = Class.forName(jobClassStr);
            String name = jobClassStr + config.getId();
            String groupName = config.getGroupName();
            String description = config.toString();
            String time = config.getUpdateAt();

            JobDetail jobDetail = createJobDetail(clazz, name, groupName, description);
            if (jobDetail == null) {
                return false;
            }
            Trigger trigger = createCronTrigger(jobDetail, time, name, groupName, description);
            if (trigger == null) {
                return false;
            }

            jobFactory.setApplicationContext(context);

            schedulerFactoryBean.setJobFactory(jobFactory);
            schedulerFactoryBean.setJobDetails(jobDetail);
            schedulerFactoryBean.setTriggers(trigger);
            schedulerFactoryBean.afterPropertiesSet();
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * create JobDetail
     * @param clazz job class
     * @param name  job name
     * @param groupName  group name
     * @param description  details
     * @return  JobDetail entity
     */
    private  static JobDetail createJobDetail(Class clazz, String name, String groupName, String description) {
        jobDetailFactory.setJobClass(clazz);
        jobDetailFactory.setName(name);
        jobDetailFactory.setGroup(groupName);
        jobDetailFactory.setDescription(description);
        jobDetailFactory.setDurability(true);
        jobDetailFactory.afterPropertiesSet();
        return jobDetailFactory.getObject();
    }

    /**
     * create CronTrigger
     *
     * @param job job detail
     * @param time  execute time
     * @param name trigger name
     * @param groupName group name
     * @param description trigger description
     * @return CronTrigger
     */
    private  static CronTrigger createCronTrigger(JobDetail job, String time, String name, String groupName, String description) {
        factoryBean.setName(name);
        factoryBean.setJobDetail(job);
        factoryBean.setCronExpression(time);
        factoryBean.setDescription(description);
        factoryBean.setGroup(groupName);
        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return factoryBean.getObject();
    }
}
