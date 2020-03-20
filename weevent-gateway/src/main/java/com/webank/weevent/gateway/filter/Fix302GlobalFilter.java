package com.webank.weevent.gateway.filter;


import java.net.URI;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Fix 302 redirect bug in location.
 *
 * @author matthewliu
 * @since 2020/02/20
 */
@Slf4j
public class Fix302GlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUri = exchange.getRequest().getURI();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                switch (getStatusCode()) {
                    case MOVED_PERMANENTLY: //301
                    case FOUND: //302
                        HttpHeaders headers = getHeaders();
                        String location = headers.getFirst(HttpHeaders.LOCATION);
                        if (!StringUtils.isEmpty(location)) {
                            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(location);
                            // replace scheme//host:port with original request's
                            uriComponentsBuilder.scheme(requestUri.getScheme())
                                    .host(requestUri.getHost())
                                    .port(requestUri.getPort());
                            String newLocation = uriComponentsBuilder.build().toUri().toString();
                            headers.put(HttpHeaders.LOCATION, Collections.singletonList(newLocation));

                            log.info("301/302 redirect in R: {}, FIX location {} -> {}", requestUri, location, newLocation);
                        }
                        break;

                    default:
                        break;
                }

                return super.writeWith(body);
            }
        };

        // replace response with decorator
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }
}
