package com.webank.weevent.filter;


import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
        ServerHttpRequest request = exchange.getRequest();
        log.info("{} R: {} {} {}",
                request.getRemoteAddress(),
                request.getId(),
                request.getMethodValue(),
                request.getURI());

        long startTimestamp = Calendar.getInstance().getTimeInMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            log.info("R: {}, {} {}(ms)",
                    request.getId(),
                    response.getStatusCode(),
                    Calendar.getInstance().getTimeInMillis() - startTimestamp);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
