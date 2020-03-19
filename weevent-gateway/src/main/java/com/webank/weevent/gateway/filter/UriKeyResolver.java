package com.webank.weevent.gateway.filter;


import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Uri as key.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
public class UriKeyResolver implements KeyResolver {
    public static final String BEAN_NAME = "UriKeyResolver";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getURI().toString());
    }
}
