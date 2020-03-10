package com.webank.weevent.processor.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.processor.ProcessorApplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.ResponseEntity;

@Slf4j
public class SaveTopicDataUtil {


    private final static String serviceId = "weevent-governance";

    public final static String saveTopicUrl = "/historicalData/insertHistoricalData";

    public static boolean saveTopicData(Map<String, String> sqlvalue) throws BrokerException {
        try {
            Map<String, String> topicHashMap = new HashMap<>();
            topicHashMap.put(ConstantsHelper.EVENT_ID, sqlvalue.get(ConstantsHelper.EVENT_ID));
            topicHashMap.put(ConstantsHelper.BROKER_ID, sqlvalue.get(ConstantsHelper.BROKER_ID));
            topicHashMap.put(ConstantsHelper.GROUP_ID, sqlvalue.get(ConstantsHelper.GROUP_ID));
            topicHashMap.put(ConstantsHelper.TOPIC_NAME, sqlvalue.get(ConstantsHelper.TOPIC_NAME));

            String urlFromDiscovery = getUrlFromDiscovery();
            String url = urlFromDiscovery + "/" + serviceId + saveTopicUrl;
            ResponseEntity<Map> mapResponseEntity = ProcessorApplication.restTemplate.postForEntity(url, topicHashMap, Map.class);
            Map body = mapResponseEntity.getBody();
            log.info("insert result", body.get("result"));
            return (boolean) body.get("result");
        } catch (Exception e) {
            log.info("insert fail", e);
            return false;
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
