package com.webank.weevent.filter;


import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * Route to fixed instance following http header "file_host".
 *
 * @author matthewliu
 * @since 2020/02/20
 */
@Slf4j
@Component
public class FixedHostFilter extends LoadBalancerClientFilter {
    private DiscoveryClient discoveryClient;

    public FixedHostFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties, DiscoveryClient discoveryClient) {
        super(loadBalancer, properties);
        this.discoveryClient = discoveryClient;
    }

    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        String fileHost = httpHeaders.getFirst("file_host");
        if (!StringUtils.isEmpty(fileHost)) {
            List<ServiceInstance> instances = this.discoveryClient.getInstances("weevent-broker");
            for (ServiceInstance instance : instances) {
                if (instance.getInstanceId().equals(fileHost)) {
                    log.info("FIND header \"file_host\": {}, FIX choose it", fileHost);
                    return instance;
                }
            }
        }

        log.info("FIND header \"file_host\": {}, but it's invalid instance, skip this", fileHost);
        return null;
    }
}
