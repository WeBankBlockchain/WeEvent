package com.webank.weevent.gateway.filter;


import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

/**
 * limit in local memory.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
public class LocalMemoryLimiter extends AbstractRateLimiter<LocalMemoryLimiter.Config> {
    /*
    filters:
    - name: RequestRateLimiter
      args:
        keyResolver: "#{@UriKeyResolver}"
        local-memory-rate-limiter:
          replenish-rate: 5
          burst-capacity: 10
    */
    // kebab-case
    public static final String CONFIGURATION_PROPERTY_NAME = "local-memory-rate-limiter";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LocalMemoryLimiter() {
        super(Config.class, CONFIGURATION_PROPERTY_NAME, null);
    }

    @NotNull
    public Map<String, String> getHeaders(Config config, long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RedisRateLimiter.REMAINING_HEADER, String.valueOf(tokensLeft));
        headers.put(RedisRateLimiter.REPLENISH_RATE_HEADER,
                String.valueOf(config.getReplenishRate()));
        headers.put(RedisRateLimiter.BURST_CAPACITY_HEADER,
                String.valueOf(config.getBurstCapacity()));
        return headers;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Config routeConfig = getConfig().get(routeId);
        if (routeConfig == null) {
            throw new IllegalArgumentException("No Configuration found for route " + routeId);
        }

        // How many requests per second do you want a user to be allowed to do?
        int replenishRate = routeConfig.getReplenishRate();
        // How much bursting do you want to allow?
        int burstCapacity = routeConfig.getBurstCapacity();

        Bucket bucket = this.buckets.computeIfAbsent(id, k -> {
            Refill refill = Refill.greedy(replenishRate, Duration.ofSeconds(1));
            Bandwidth limit = Bandwidth.classic(burstCapacity, refill);
            return Bucket4j.builder().addLimit(limit).build();
        });

        // tryConsume returns false immediately if no tokens available with the bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(10);
        if (probe.isConsumed()) {
            // the limit is not exceeded
            return Mono.just(new Response(true, Collections.emptyMap()));
        } else {
            // limit is exceeded
            return Mono.just(new Response(false, getHeaders(routeConfig, 0)));
        }
    }

    @Validated
    public static class Config {
        @Min(1)
        private int replenishRate;

        @Min(0)
        private int burstCapacity = 0;

        public int getReplenishRate() {
            return replenishRate;
        }

        public LocalMemoryLimiter.Config setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
            return this;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public LocalMemoryLimiter.Config setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
            return this;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "replenishRate=" + replenishRate +
                    ", burstCapacity=" + burstCapacity +
                    '}';
        }
    }
}
