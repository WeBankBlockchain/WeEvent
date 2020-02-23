package com.webank.weevent;


import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Record log for every request.
 *
 * @author matthewliu
 * @since 2020/01/20
 */
@Slf4j
public class LogGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTimestamp = Calendar.getInstance().getTimeInMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("{} {}(ms) {} {}, {} {} {}",
                    request.getRemoteAddress(),
                    Calendar.getInstance().getTimeInMillis() - startTimestamp,
                    request.getURI().getScheme(),
                    exchange.getResponse().getStatusCode(),
                    request.getMethodValue(),
                    request.getPath(),
                    request.getQueryParams());
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
