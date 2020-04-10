package com.webank.weevent.gateway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.webank.weevent.gateway.dto.NamingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Show information in service naming register.
 *
 * @author matthewliu
 * @since 2020/04/10
 */
@Slf4j
@RestController
// netty DO NOT support context path
@RequestMapping(value = "/${spring.application.name}/naming")
public class NamingController {
    private DiscoveryClient discoveryClient;

    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    // get all services in register
    @RequestMapping("/list")
    public Mono<Map<String, List<NamingService>>> list() {
        Map<String, List<NamingService>> output = new HashMap<>();
        List<String> services = this.discoveryClient.getServices();
        for (String service : services) {
            List<ServiceInstance> serviceInstances = this.discoveryClient.getInstances(service);
            output.put(service, serviceInstances.stream().map(NamingService::convert).collect(Collectors.toList()));
        }
        return Mono.just(output);
    }
}
