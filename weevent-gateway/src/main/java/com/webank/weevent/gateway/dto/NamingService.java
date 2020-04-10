package com.webank.weevent.gateway.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.client.ServiceInstance;

/**
 * Simplify for org.springframework.cloud.client.ServiceInstance
 *
 * @author matthewliu
 * @since 2020/04/10
 */
@Getter
@Setter
public class NamingService {
    private String serviceId;
    private String instanceId;
    private boolean secure;
    private String host;
    private int port;
    private String uri;

    public static NamingService convert(ServiceInstance instance) {
        NamingService namingService = new NamingService();
        namingService.serviceId = instance.getServiceId();
        namingService.instanceId = instance.getInstanceId();
        namingService.host = instance.getHost();
        namingService.port = instance.getPort();
        namingService.uri = instance.getUri().toString();
        namingService.secure = instance.isSecure();

        return namingService;
    }
}
