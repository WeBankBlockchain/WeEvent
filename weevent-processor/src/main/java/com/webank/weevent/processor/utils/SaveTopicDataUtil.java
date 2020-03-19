package com.webank.weevent.processor.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.webank.weevent.client.WeEvent;
import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.model.CEPRule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.ResponseEntity;

@Slf4j
public class SaveTopicDataUtil {


    private final static String serviceId = "weevent-governance";

    private final static String governanceServiceUrl = "http://127.0.0.1:7009";

    public final static String saveTopicUrl = "/historicalData/insertHistoricalData";

    public static String saveTopicData(WeEvent eventContent, CEPRule rule) {
        Map<String, Object> topicHashMap = new HashMap<>();
        try {
            topicHashMap.put(ConstantsHelper.WEEVENT, eventContent);
            topicHashMap.put(ConstantsHelper.BROKER_ID, rule.getBrokerId());
            topicHashMap.put(ConstantsHelper.GROUP_ID, rule.getGroupId());
            String urlFromDiscovery = getUrlFromDiscovery();
            String url = urlFromDiscovery + "/" + serviceId + saveTopicUrl;
            ResponseEntity<Boolean> mapResponseEntity = ProcessorApplication.restTemplate.postForEntity(url, topicHashMap, Boolean.class);
            Boolean flag = mapResponseEntity.getBody();
            log.info("insert result,{}", flag);
            if (flag) {
                return ConstantsHelper.WRITE_DB_SUCCESS;
            } else {
                return ConstantsHelper.WRITE_DB_FAIL;
            }
        } catch (Exception e) {
            log.info("insert fail", e);
            return ConstantsHelper.WRITE_DB_FAIL;
        }

    }

    public static String getUrlFromDiscovery() {
        List<ServiceInstance> serviceInstances = ProcessorApplication.discoveryClient.getInstances(serviceId);
        if (serviceInstances.isEmpty()) {
            return governanceServiceUrl;
        }
        ServiceInstance serviceInstance = serviceInstances.get(new Random().nextInt(serviceInstances.size()));
        return serviceInstance.getUri().toString();
    }
}
