package com.webank.weevent.governance.utils;


import java.util.List;
import java.util.Random;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Utils tools.
 *
 * @author matthewliu
 * @since 2020/02/09
 */
public class Utils {
    public static String getUrlFromDiscovery(DiscoveryClient discoveryClient, String serviceId) {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
        if (serviceInstances.isEmpty()) {
            return "";
        }
        ServiceInstance serviceInstance = serviceInstances.get(new Random().nextInt(serviceInstances.size()));
        return serviceInstance.getUri().toString();
    }
}
