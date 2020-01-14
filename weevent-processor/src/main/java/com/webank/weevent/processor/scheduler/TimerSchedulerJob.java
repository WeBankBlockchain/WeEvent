package com.webank.weevent.processor.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.webank.weevent.processor.repository.TimerSchedulerRepository;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class TimerSchedulerJob {

    @Autowired
    private TimerSchedulerRepository schedulerRepository;

    private Map<Integer, Timer> timerMap = new ConcurrentHashMap<>();
    private Map<Integer, TimerScheduler> timerSchedulerMap = new ConcurrentHashMap<>();

    private final String jdbcUrl = "jdbc:h2:tcp://localhost:7082/~/WeEvent_governance?user=root&password=123456";
    private final long timePeriod = 1000L;
    private final String parsingSql = "select * from t_topic_historical";
    private final String schedulerName = "systemScheduler";

    /**
     * 以任务id为key，任务为value，存储到map里面
     * 1.每次新增，先入库，创建一个任务
     * 2.每次修改,去map里面取任务，修改，同时更新数据库状态
     * 3.删除任务，停止任务，删除数据库记录
     * 4.查询任务也从map取
     * 5.项目停止再重启，去数据库查询任务重新拉起任务
     * 6.以历史数据为例，需要有一个记录上次同步的时间，每次都是解析过去24个小时之内的数据
     * 7.历史数据的可以内置一个定时器
     */


    @PostConstruct
    public void initTimerScheduler() throws BrokerException {
        TimerScheduler timerScheduler = new TimerScheduler();
        List<TimerScheduler> timerSchedulerList = this.timerSchedulerList(timerScheduler);
        if (timerSchedulerList.isEmpty()) {
            /**
             * create build-in task
             */
            TimerScheduler scheduler = new TimerScheduler(schedulerName, jdbcUrl, timePeriod, parsingSql, new Date(), new Date());
            this.insertTimerScheduler(timerScheduler);
            return;
        }
        for (TimerScheduler scheduler : timerSchedulerList) {
            timerSchedulerMap.put(scheduler.getId(), scheduler);
            Timer task = createTask(scheduler);
            timerMap.put(scheduler.getId(), task);
        }
    }


    @Transactional(rollbackFor = Throwable.class)
    public TimerScheduler insertTimerScheduler(TimerScheduler timerScheduler) throws BrokerException {
        try {
            //1.save entity
            timerScheduler.setCreatedTime(new Date());
            timerScheduler.setUpdatedTime(new Date());
            timerScheduler = schedulerRepository.save(timerScheduler);
            timerSchedulerMap.put(timerScheduler.getId(), timerScheduler);
            //2.create task
            Timer task = this.createTask(timerScheduler);
            timerMap.put(timerScheduler.getId(), task);
            log.info("insert timerScheduler success");
            return timerScheduler;
        } catch (Exception e) {
            log.error("insert timerScheduler fail", e);
            throw new BrokerException("insert timerScheduler fail", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public TimerScheduler updateTimerScheduler(TimerScheduler timerScheduler) throws BrokerException {
        try {
            //update timerScheduler
            TimerScheduler oldScheduler = schedulerRepository.findById(timerScheduler.getId());
            BeanUtils.copyProperties(timerScheduler, oldScheduler, "createdTime");
            oldScheduler.setUpdatedTime(new Date());
            timerSchedulerMap.put(oldScheduler.getId(), oldScheduler);
            //stop old task
            this.stopTask(timerMap.get(timerScheduler.getId()));
            timerMap.remove(timerScheduler.getId());
            //create new task
            Timer timerTask = this.createTask(timerScheduler);
            timerMap.put(oldScheduler.getId(), timerTask);
            timerScheduler = schedulerRepository.save(oldScheduler);
            log.info("update timerScheduler success");
            return timerScheduler;
        } catch (Exception e) {
            log.error("update timerScheduler fail", e);
            throw new BrokerException("update timerScheduler fail", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteTimerScheduler(TimerScheduler timerScheduler) throws BrokerException {
        try {
            if (timerMap.get(timerScheduler.getId()) != null) {
                timerSchedulerMap.remove(timerScheduler.getId());
                this.stopTask(timerMap.get(timerScheduler.getId()));
                schedulerRepository.delete(timerScheduler);

            }
            log.info("delete timerScheduler success");
        } catch (Exception e) {
            log.error("delete timerScheduler fail", e);
            throw new BrokerException("delete timerScheduler fail," + e.getMessage());
        }
    }

    public List<TimerScheduler> timerSchedulerList(TimerScheduler timerScheduler) throws BrokerException {
        try {
            Example<TimerScheduler> example = Example.of(timerScheduler);
            return schedulerRepository.findAll(example);
        } catch (Exception e) {
            log.error("insert timerScheduler fail", e);
            throw new BrokerException("insert timerScheduler fail", e);
        }
    }

    private Timer createTask(TimerScheduler timerScheduler) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();

        /*** 定制每日2:00执行方法 ***/
        Map<String, Integer> periodMap = timerScheduler.getPeriodMap();
        calendar.set(Calendar.HOUR_OF_DAY, periodMap.get("year"));
        calendar.set(Calendar.HOUR_OF_DAY, periodMap.get("month"));
        calendar.set(Calendar.HOUR_OF_DAY, periodMap.get("day"));
        calendar.set(Calendar.HOUR_OF_DAY, periodMap.get("week"));
        calendar.set(Calendar.HOUR_OF_DAY, periodMap.get("hour"));
        calendar.set(Calendar.MINUTE, periodMap.get("minute"));
        calendar.set(Calendar.SECOND, periodMap.get("second"));

        timer.schedule(new TimerSchedulerTask(timerScheduler), timerScheduler.getTimePeriod());
        return timer;
    }

    private void stopTask(Timer timer) {
        timer.cancel();
        timer.purge();
    }

}


