package com.webank.weevent.processor.repository;

import com.webank.weevent.processor.model.TimerScheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimerSchedulerRepository extends JpaRepository<TimerScheduler, Long> {

    TimerScheduler findById(Integer id);

}
