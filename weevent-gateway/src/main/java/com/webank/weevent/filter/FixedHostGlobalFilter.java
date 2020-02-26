package com.webank.weevent.filter;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Fix 302 redirect bug in location.
 *
 * @author matthewliu
 * @since 2020/02/20
 */
@Slf4j
@Component
public class FixedHostGlobalFilter extends RouteToRequestUrlFilter {
    private DiscoveryClient discoveryClient;

    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public int getOrder() {
        return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        String fileHost = httpHeaders.getFirst("file_host");
        if (!StringUtils.isEmpty(fileHost)) {
            ServiceInstance found = null;
            List<ServiceInstance> instances = this.discoveryClient.getInstances(fileHost);
            for (ServiceInstance instance : instances) {
                if (instance.getInstanceId().equals(fileHost)) {
                    found = instance;
                    break;
                }
            }

            if (found != null) {
                try {
                    URI requestUri = exchange.getRequest().getURI();
                    // replace scheme//host:port in request with instance's
                    URI uri = new URI(found.getScheme(),
                            requestUri.getUserInfo(),
                            found.getHost(),
                            found.getPort(),
                            requestUri.getPath(),
                            requestUri.getQuery(),
                            requestUri.getFragment());
                    String newUrl = uri.toString();

                    log.info("FIND file_host: {} in header, FIX URL {} -> {}", fileHost, requestUri.toString(), newUrl);
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUrl);
                } catch (URISyntaxException e) {
                    log.error("invalid uri format: {}", e.getMessage());
                }
            }
        }

        return super.filter(exchange, chain);
    }
}
