package com.webank.weevent.broker.fabric.sdk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fisco.RedisService;
import com.webank.weevent.broker.fisco.util.LRUCache;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/23
 */
@Slf4j
@Data
public class FabricDelegate {
    private Map<String, Fabric> fabricMap;

    // block data cached in redis
    private static RedisService redisService;

    // block data cached in local memory
    private static LRUCache<String, List<WeEvent>> blockCache;

    public FabricDelegate() {
        this.fabricMap = new ConcurrentHashMap<>();
    }

    private void initRedisService() {
        if (redisService == null) {
            // load redis service if needed
            String redisServerIp = BrokerApplication.weEventConfig.getRedisServerIp();
            Integer redisServerPort = BrokerApplication.weEventConfig.getRedisServerPort();
            if (StringUtils.isNotBlank(redisServerIp) && redisServerPort > 0) {
                redisService = BrokerApplication.applicationContext.getBean(RedisService.class);
            }
        }

        if (blockCache == null) {
            // skip local cache if lru.cache.capacity = 0
            Integer capacity = BrokerApplication.weEventConfig.getMaxCapacity();
            if (capacity > 0) {
                blockCache = new LRUCache<>(capacity);
            }
        }
    }

    public void initProxy(FabricConfig fabricConfig) throws BrokerException {
        Fabric fabric = new Fabric(fabricConfig);
        fabric.init(fabricConfig.getChannelName());
        fabricMap.put(fabricConfig.getChannelName(), fabric);
    }

    public SendResult publishEvent(String topicName, String channelName, String eventContent, String extensions) throws BrokerException {
        return this.fabricMap.get(channelName).publishEvent(topicName, eventContent, extensions);
    }

    public List<String> listChannel() throws BrokerException {
        return fabricMap.get(WeEvent.DEFAULT_CHANNEL_NAME).listChannelName();
    }

    public Long getBlockHeight(String channelName) throws BrokerException {
        return this.fabricMap.get(channelName).getBlockHeight();
    }

    public List<WeEvent> loop(Long blockNum, String channelName) throws BrokerException {
        return fabricMap.get(channelName).loop(blockNum);
    }
}
