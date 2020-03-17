package com.webank.weevent.processor.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.model.CEPRule;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.ResponseEntity;

@Slf4j
public class SaveTopicDataUtil {


    private final static String serviceId = "weevent-governance";

    public final static String saveTopicUrl = "/historicalData/insertHistoricalData";

    public static String saveTopicData(WeEvent eventContent, CEPRule rule) {
        Map<String, Object> topicHashMap = new HashMap<>();
        try {
            Map<String, String> sqlvalue = CommonUtil.contactsql(rule, eventContent);
            Map<String, String> extensions = eventContent.getExtensions();
            if (extensions.get(WeEvent.WeEvent_PLUS) != null) {
                Map<String, String> map = JsonHelper.json2Object(extensions.get("weevent-plus"), new TypeReference<Map<String, String>>() {
                });
                long timestamp = Long.parseLong(map.get("timestamp"));
                topicHashMap.put(ConstantsHelper.CREATE_DATE, new Date(timestamp));
            }
            topicHashMap.put(ConstantsHelper.EVENT_ID, sqlvalue.get(ConstantsHelper.EVENT_ID));
            topicHashMap.put(ConstantsHelper.BROKER_ID, sqlvalue.get(ConstantsHelper.BROKER_ID));
            topicHashMap.put(ConstantsHelper.GROUP_ID, sqlvalue.get(ConstantsHelper.GROUP_ID));
            topicHashMap.put(ConstantsHelper.TOPIC_NAME, sqlvalue.get(ConstantsHelper.TOPIC_NAME));
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
            return "";
        }
        ServiceInstance serviceInstance = serviceInstances.get(new Random().nextInt(serviceInstances.size()));
        return serviceInstance.getUri().toString();
    }
}
