package com.webank.weevent.processor.quartz;

import java.util.Date;
import java.util.List;

import com.webank.weevent.processor.model.TimerScheduler;
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

    @Transactional(rollbackFor = Throwable.class)
    public TimerScheduler insertTimerScheduler(TimerScheduler timerScheduler) throws BrokerException {
        try {
            timerScheduler.setCreatedTime(new Date());
            timerScheduler.setUpdatedTime(new Date());
            timerScheduler = schedulerRepository.save(timerScheduler);
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
            TimerScheduler oldScheduler = schedulerRepository.findById(timerScheduler.getId());
            BeanUtils.copyProperties(timerScheduler, oldScheduler, "createdTime");
            oldScheduler.setUpdatedTime(new Date());
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
            schedulerRepository.delete(timerScheduler);
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

}
