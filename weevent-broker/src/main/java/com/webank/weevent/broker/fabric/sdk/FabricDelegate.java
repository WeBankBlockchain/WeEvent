package com.webank.weevent.broker.fabric.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fisco.util.LRUCache;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/23
 */
@Slf4j
public class FabricDelegate {
    private Map<String, Fabric> fabricMap;

    // block data cached in redis
    private static RedisTemplate<String, List<WeEvent>> redisTemplate;

    // block data cached in local memory
    private static LRUCache<String, List<WeEvent>> blockCache;

    private static List<String> channels = new ArrayList<>();

    // fabricConfig
    private FabricConfig fabricConfig;

    public FabricDelegate() {
        this.fabricMap = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private void initRedisService() {
        try {
            redisTemplate = BrokerApplication.applicationContext.getBean("springRedisTemplate", RedisTemplate.class);
        } catch (BeansException e) {
            log.info("No redis service is configured");
        }

        if (blockCache == null) {
            // skip local cache if lru.cache.capacity = 0
            Integer capacity = BrokerApplication.weEventConfig.getMaxCapacity();
            if (capacity > 0) {
                blockCache = new LRUCache<>(capacity);
            }
        }
    }

    public void initProxy(FabricConfig config) throws BrokerException {
        this.fabricConfig = config;
        Fabric fabric = new Fabric(config);
        fabric.init(config.getChannelName());
        fabricMap.put(config.getChannelName(), fabric);
        channels = fabric.listChannelName(config);
        initRedisService();
    }

    public CompletableFuture<SendResult> publishEvent(String topicName, String channelName, String eventContent, String extensions) throws BrokerException {

        return this.fabricMap.get(channelName).publishEvent(topicName, eventContent, extensions);
    }

    public List<String> listChannel() {
        return channels;
    }

    public Long getBlockHeight(String channelName) throws BrokerException {
        return this.fabricMap.get(channelName).getBlockHeight();
    }

    public List<WeEvent> loop(Long blockNum, String channelName) {
        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        // try to get data from local cache and redis
        String key = getRedisKey(blockNum, channelName);
        events = getFromCache(key);
        // redis data may be dirty
        if (events != null) {
            return events;
        }
        events = this.fabricMap.get(channelName).loop(blockNum);

        //write events list to redis server
        setCache(key, events);
        return events;
    }

    public static String getChannelName() {
        return channels.get(0);
    }

    public Map<String, Fabric> getFabricMap() {
        return this.fabricMap;
    }

    public FabricConfig getFabricConfig() {
        return this.fabricConfig;
    }

    private String getRedisKey(Long blockNum, String channelName) {
        return channelName.concat(Long.toString(blockNum));
    }

    private List<WeEvent> getFromCache(String key) {
        try {
            if (blockCache != null && blockCache.containsKey(key)) {
                return blockCache.get(key);
            }
            if (redisTemplate != null) {
                return redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            log.error("Exception happened while read events from redis server", e);
        }

        return null;
    }

    private void setCache(String key, List<WeEvent> events) {
        try {
            if (events != null) {
                if (blockCache != null) {
                    blockCache.putIfAbsent(key, events);
                }
                if (redisTemplate != null) {
                    redisTemplate.opsForValue().set(key, events);
                }
            }
        } catch (Exception e) {
            log.error("Exception happened while write events to redis server", e);
        }
    }

}
