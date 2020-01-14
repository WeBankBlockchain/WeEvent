package com.webank.weevent.processor.scheduler;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.repository.TimerSchedulerRepository;
import com.webank.weevent.processor.utils.JsonUtil;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String userName;

    @Value("${spring.datasource.password}")
    private String password;


    private final long timePeriod = 1000L;
    private final String parsingSql = "select * from t_topic_historical";
    private final String schedulerName = "systemScheduler";

    //    @PostConstruct
    public void initTimerScheduler() throws BrokerException, IOException {
        TimerScheduler timerScheduler = new TimerScheduler();
        List<TimerScheduler> timerSchedulerList = this.timerSchedulerList(timerScheduler);
        if (timerSchedulerList.isEmpty()) {
            String jdbcUrl = databaseUrl + "?user=" + userName + "&password=" + password;
            //create build-in task
            TimerScheduler scheduler = new TimerScheduler(schedulerName, jdbcUrl, timePeriod, null, null, parsingSql);
            this.insertTimerScheduler(scheduler);
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

    private Timer createTask(TimerScheduler timerScheduler) throws IOException {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();

        /*** Set what time period to execute regularly***/
        String periodParams = timerScheduler.getPeriodParams();
        Map<String, Integer> periodMap = JsonUtil.parseObjectToMap(periodParams, String.class, Integer.class);
        calendar.set(Calendar.YEAR, periodMap.get("year") == null ? calendar.get(Calendar.YEAR) : periodMap.get("year"));
        calendar.set(Calendar.MONTH, periodMap.get("month") == null ? calendar.get(Calendar.MONTH) : periodMap.get("month"));
        calendar.set(Calendar.DAY_OF_YEAR, periodMap.get("day") == null ? calendar.get(Calendar.DAY_OF_YEAR) : periodMap.get("day"));
        calendar.set(Calendar.DAY_OF_WEEK, periodMap.get("week") == null ? calendar.get(Calendar.DAY_OF_WEEK) : periodMap.get("week"));
        calendar.set(Calendar.HOUR_OF_DAY, periodMap.get("hour") == null ? calendar.get(Calendar.HOUR_OF_DAY) : periodMap.get("hour"));
        calendar.set(Calendar.MINUTE, periodMap.get("minute") == null ? calendar.get(Calendar.MINUTE) : periodMap.get("minute"));
        calendar.set(Calendar.SECOND, periodMap.get("second") == null ? calendar.get(Calendar.SECOND) : periodMap.get("second"));
        long time = calendar.getTime().getTime();

        timer.schedule(new TimerSchedulerTask(timerScheduler), timerScheduler.getDelay(), time);
        return timer;
    }

    private void stopTask(Timer timer) {
        timer.cancel();
        timer.purge();
    }

}


