package com.webank.weevent.governance.service;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.common.ConstantProperties;
import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.entity.TimerSchedulerEntity;
import com.webank.weevent.governance.repository.RuleDatabaseRepository;
import com.webank.weevent.governance.repository.TimerSchedulerRepository;
import com.webank.weevent.governance.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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

    @Autowired
    private CommonService commonService;

    private static final int PROCESSOR_SUCCESS_CODE = 0;


    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

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
            TimerSchedulerEntity schedulerEntity = timerSchedulerRepository.save(timerSchedulerEntity);
            //processor insert timerScheduler
            this.insertTimerScheduler(request, timerSchedulerEntity);
            return schedulerEntity;
        } catch (Exception e) {
            log.info("add timerScheduler failed", e);
            throw new GovernanceException("add timerScheduler failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void insertTimerScheduler(HttpServletRequest request, TimerSchedulerEntity timerSchedulerEntity) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            this.setRuleDataBaseUrl(timerSchedulerEntity);
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.TIMER_SCHEDULER_INSERT).toString();
            String jsonString = JsonHelper.object2Json(timerSchedulerEntity);
            Map map = JsonHelper.json2Object(jsonString, Map.class);
            map.put("updatedTime", timerSchedulerEntity.getLastUpdate());
            map.put("createdTime", timerSchedulerEntity.getCreateDate());
            //updateCEPRuleById
            log.info("insert timerScheduler ====map:{}", JsonHelper.object2Json(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JsonHelper.object2Json(map));
            String updateMes = EntityUtils.toString(closeResponse.getEntity());
            log.info("insert timerScheduler ====result:{}", updateMes);
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            Map jsonObject = JsonHelper.json2Object(updateMes, Map.class);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor insert timerScheduler fail", e);
            throw new GovernanceException("processor insert timerScheduler fail", e);
        }

    }

    public void updateTimerScheduler(TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        try {
            //check params
            timerSchedulerEntity.setLastUpdate(new Date());
            this.updateTimerScheduler(request, timerSchedulerEntity);
            timerSchedulerRepository.save(timerSchedulerEntity);
        } catch (Exception e) {
            log.info("update timerScheduler failed", e);
            throw new GovernanceException("update timerScheduler failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTimerScheduler(HttpServletRequest request, TimerSchedulerEntity timerSchedulerEntity) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            this.setRuleDataBaseUrl(timerSchedulerEntity);

            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.TIMER_SCHEDULER_UPDATE).toString();
            String jsonString = JsonHelper.object2Json(timerSchedulerEntity);
            Map map = JsonHelper.json2Object(jsonString, Map.class);
            map.put("updatedTime", timerSchedulerEntity.getLastUpdate());
            map.put("createdTime", timerSchedulerEntity.getCreateDate());
            //updateCEPRuleById
            log.info("update timerScheduler ====map:{}", JsonHelper.object2Json(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JsonHelper.object2Json(map));
            String updateMes = EntityUtils.toString(closeResponse.getEntity());
            log.info("update timerScheduler ====result:{}", updateMes);
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            Map jsonObject = JsonHelper.json2Object(updateMes, Map.class);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor update timerScheduler fail", e);
            throw new GovernanceException("processor update timerScheduler fail", e);
        }

    }

    public void deleteTimerScheduler(TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request) throws GovernanceException {
        try {
            Optional<TimerSchedulerEntity> optional = timerSchedulerRepository.findById(timerSchedulerEntity.getId());
            if (!optional.isPresent()) {
                return;
            }
            timerSchedulerRepository.delete(optional.get());
            this.deleteTimerScheduler(request, optional.get());
        } catch (Exception e) {
            log.info("delete timerScheduler failed", e);
            throw new GovernanceException("delete timerScheduler failed", e);
        }
    }


    public void deleteTimerScheduler(HttpServletRequest request, TimerSchedulerEntity timerSchedulerEntity) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            String deleteUrl = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.TIMER_SCHEDULER_DELETE).toString();
            log.info("processor delete timerScheduler,id:{}", timerSchedulerEntity.getId());
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, deleteUrl, JsonHelper.object2Json(timerSchedulerEntity));
            String mes = EntityUtils.toString(closeResponse.getEntity());
            log.info("delete timerScheduler result:{}", mes);
            //deal  timerScheduler result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                log.error(ErrorCode.PROCESS_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }

            Map jsonObject = JsonHelper.json2Object(mes, Map.class);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor delete timerScheduler fail ! {}", e.getMessage());
            throw new GovernanceException("processor delete timerScheduler fail ", e);
        }

    }


    public boolean checkProcessorExist(HttpServletRequest request) {
        try {
            String payload = "{\"a\":\"1\",\"b\":\"test\",\"c\":\"10\"}";
            String condition = "\"c<100\"";
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_CHECK_WHERE_CONDITION)
                    .append(ConstantProperties.QUESTION_MARK).append("payload=").append(URLEncoder.encode(payload, "UTF-8"))
                    .append("&condition=").append(URLEncoder.encode(condition, "UTF-8"))
                    .toString();
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url);
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            return 200 == statusCode;
        } catch (Exception e) {
            return false;
        }
    }

    private String getProcessorUrl() {
        return Utils.getUrlFromDiscovery(this.discoveryClient, "weevent-processor") + "/weevent-processor";
    }

    private void setRuleDataBaseUrl(TimerSchedulerEntity timerSchedulerEntity) {
        if (timerSchedulerEntity.getRuleDatabaseId() == null) {
            return;
        }
        RuleDatabaseEntity ruleDataBase = ruleDatabaseRepository.findById(timerSchedulerEntity.getRuleDatabaseId());
        if (ruleDataBase != null) {
            String dbUrl = ruleDataBase.getDatabaseUrl() + "?user=" + ruleDataBase.getUsername() + "&password=" + ruleDataBase.getPassword();
            if (!StringUtil.isBlank(ruleDataBase.getOptionalParameter())) {
                dbUrl = dbUrl + "&" + ruleDataBase.getOptionalParameter();
            }
            timerSchedulerEntity.setDatabaseUrl(dbUrl);
            timerSchedulerEntity.setDataBaseType(ruleDataBase.getDatabaseType());
            log.info("dataBaseUrl:{}", timerSchedulerEntity.getDatabaseUrl());
        }
    }
}
