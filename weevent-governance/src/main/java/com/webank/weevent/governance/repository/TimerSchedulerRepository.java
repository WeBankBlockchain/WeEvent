package com.webank.weevent.governance.repository;

import com.webank.weevent.governance.entity.TimerSchedulerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimerSchedulerRepository extends JpaRepository<TimerSchedulerEntity, Integer> {


}
