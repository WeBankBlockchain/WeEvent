package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.entity.TimerSchedulerEntity;
import com.webank.weevent.governance.repository.RuleDatabaseRepository;
import com.webank.weevent.governance.repository.TimerSchedulerRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TimerSchedulerService {

    @Autowired
    private TimerSchedulerRepository timerSchedulerRepository;

    @Autowired
    private RuleDatabaseRepository ruleDatabaseRepository;

    public List<TimerSchedulerEntity> getTimerSchedulerList(HttpServletRequest request, TimerSchedulerEntity timerSchedulerEntity) throws GovernanceException {
        try {
            Example<TimerSchedulerEntity> example = Example.of(timerSchedulerEntity);
            long count = timerSchedulerRepository.count(example);
            if (count == 0) {
                return null;
            }
            timerSchedulerEntity.setTotalCount((int) count);

            Pageable pageable = PageRequest.of(timerSchedulerEntity.getPageNumber() - 1, timerSchedulerEntity.getPageSize());
            Page<TimerSchedulerEntity> page = timerSchedulerRepository.findAll(example, pageable);
            List<TimerSchedulerEntity> content = page.getContent();

            Set<Integer> ruleBaseIds = new HashSet<>();
            content.forEach(it -> ruleBaseIds.add(it.getRuleDatabaseId()));
            List<RuleDatabaseEntity> ruleDatabaseEntities = ruleDatabaseRepository.findAllByIdIn(ruleBaseIds);
            if (!ruleDatabaseEntities.isEmpty()) {
                Map<Integer, String> integerStringMap = ruleDatabaseEntities.stream().collect(Collectors.toMap(RuleDatabaseEntity::getId, RuleDatabaseEntity::getDatabaseUrl));
                content.forEach(it -> it.setDatabaseUrl(integerStringMap.get(it.getRuleDatabaseId())));
            }
            return content;
        } catch (Exception e) {
            log.info("find timerScheduler failed", e);
            throw new GovernanceException("find timerScheduler failed", e);
        }
    }

    public TimerSchedulerEntity addTimerScheduler(TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        try {
            return timerSchedulerRepository.save(timerSchedulerEntity);
        } catch (Exception e) {
            log.info("add timerScheduler failed", e);
            throw new GovernanceException("add timerScheduler failed", e);
        }
    }

    public void updateTimerScheduler(TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        try {
            //check params
            timerSchedulerEntity.setLastUpdate(new Date());
            timerSchedulerRepository.save(timerSchedulerEntity);
        } catch (Exception e) {
            log.info("update timerScheduler failed", e);
            throw new GovernanceException("update timerScheduler failed", e);
        }
    }

    public void deleteTimerScheduler(TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request) throws GovernanceException {
        try {
            Optional<TimerSchedulerEntity> optional = timerSchedulerRepository.findById(timerSchedulerEntity.getId());
            if (!optional.isPresent()) {
                return;
            }
            timerSchedulerRepository.delete(optional.get());
        } catch (Exception e) {
            log.info("delete timerScheduler failed", e);
            throw new GovernanceException("delete timerScheduler failed", e);
        }
    }
}
