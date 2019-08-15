package com.webank.weevent.broker.fisco.web3sdk;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.RedisService;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.LRUCache;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Detect FISCO-BCOS version from configuration 'fisco.properties' and then proxy all the invoke to the target.
 * Parameter groupId in all interface:
 * a. default 1L in 1.3.x
 * b. default 1L in 2.x, meanings first group
 * There is 2 different caches for block data. One is local memory, another is redis.
 * All can be opened/closed by configuration. And is independent to each other.
 *
 * @author matthewliu
 * @since 2019/04/28
 */
@Slf4j
public class FiscoBcosDelegate {
    // access to version 1.x
    private FiscoBcos fiscoBcos;

    // access to version 2.x
    private Map<Long, FiscoBcos2> fiscoBcos2Map;

    // web3sdk thread pool
    public static ThreadPoolTaskExecutor threadPool;

    // block data cached in redis
    private static RedisService redisService;

    // block data cached in local memory
    private static LRUCache<String, List<WeEvent>> blockCache;

    public FiscoBcosDelegate() {
        this.fiscoBcos2Map = new ConcurrentHashMap<>();
    }

    private void initRedisService() {
        if (redisService == null) {
            // load redis service if needed
            String redisServerIp = BrokerApplication.weEventConfig.getRedisServerIp();
            Integer redisServerPort = BrokerApplication.weEventConfig.getRedisServerPort();
            if (StringUtils.isNotBlank(redisServerIp) && redisServerPort > 0) {
                log.info("init redis service");

                redisService = BrokerApplication.applicationContext.getBean(RedisService.class);
            }
        }

        if (blockCache == null) {
            // skip local cache if lru.cache.capacity = 0
            Integer capacity = BrokerApplication.weEventConfig.getMaxCapacity();
            if (capacity > 0) {
                log.info("init local memory cache: " + capacity);

                blockCache = new LRUCache<>(capacity);
            }
        }
    }

    public static ThreadPoolTaskExecutor initThreadPool(FiscoConfig fiscoConfig) {
        // init thread pool
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("web3sdk_");
        pool.setCorePoolSize(fiscoConfig.getWeb3sdkCorePoolSize());
        pool.setMaxPoolSize(fiscoConfig.getWeb3sdkMaxPoolSize());
        pool.setQueueCapacity(fiscoConfig.getWeb3sdkQueueSize());
        pool.setKeepAliveSeconds(fiscoConfig.getWeb3sdkKeepAliveSeconds());
        // abort policy
        pool.setRejectedExecutionHandler(null);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init ThreadPoolTaskExecutor");
        return pool;
    }

    public void initProxy(FiscoConfig fiscoConfig) throws BrokerException {
        threadPool = initThreadPool(fiscoConfig);

        if (fiscoConfig.getVersion().startsWith("1.3")) {
            log.info("Notice: FISCO-BCOS's version is 1.x");

            // set web3sdk.Async thread pool
            new org.bcos.web3j.utils.Async(threadPool);

            FiscoBcos fiscoBcos = new FiscoBcos(fiscoConfig);
            fiscoBcos.init(fiscoConfig.getTopicControllerAddress());

            this.fiscoBcos = fiscoBcos;
        } else if (fiscoConfig.getVersion().startsWith("2.")) {
            log.info("Notice: FISCO-BCOS's version is 2.x");

            // set web3sdk.Async thread pool
            new org.fisco.bcos.web3j.utils.Async(threadPool);

            String[] tokens = fiscoConfig.getTopicControllerAddress().split(";");
            for (String token : tokens) {
                String[] groups = token.split(":");
                if (groups.length != 2) {
                    log.error("invalid address format, like 1:0xa;2:0xb");
                    throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
                }
                Long groupId = Long.valueOf(groups[0]);
                FiscoBcos2 fiscoBcos2 = new FiscoBcos2(fiscoConfig);
                fiscoBcos2.init(groupId, groups[1]);

                this.fiscoBcos2Map.put(groupId, fiscoBcos2);
            }
        } else {
            log.error("unknown FISCO-BCOS's version");
            throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
        }

        initRedisService();
    }

    /**
     * list all group id
     *
     * @return list of groupId
     */
    public Set<Long> listGroupId() {
        if (this.fiscoBcos != null) {
            Set<Long> list = new HashSet<>();
            list.add(Long.valueOf(WeEventConstants.DEFAULT_GROUP_ID));
            return list;
        } else {
            return this.fiscoBcos2Map.keySet();
        }
    }

    private void checkVersion(Long groupId) throws BrokerException {
        if (this.fiscoBcos != null) {
            if (groupId != Long.parseLong(WeEventConstants.DEFAULT_GROUP_ID)) {
                throw new BrokerException(ErrorCode.WE3SDK_VERSION_NOT_SUPPORT);
            }
            return;
        }

        if (!this.fiscoBcos2Map.containsKey(groupId)) {
            throw new BrokerException(ErrorCode.WE3SDK_UNKNOWN_GROUP);
        }
    }

    public boolean createTopic(String topicName, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.createTopic(topicName);
        } else {
            return this.fiscoBcos2Map.get(groupId).createTopic(topicName);
        }
    }

    public boolean isTopicExist(String topicName, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.isTopicExist(topicName);
        } else {
            return this.fiscoBcos2Map.get(groupId).isTopicExist(topicName);
        }
    }

    public ListPage listTopicName(Integer pageIndex, Integer pageSize, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.listTopicName(pageIndex, pageSize);
        } else {
            return this.fiscoBcos2Map.get(groupId).listTopicName(pageIndex, pageSize);
        }
    }

    public TopicInfo getTopicInfo(String topicName, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.getTopicInfo(topicName);
        } else {
            return this.fiscoBcos2Map.get(groupId).getTopicInfo(topicName);
        }
    }

    public WeEvent getEvent(String eventId, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.getEvent(eventId);
        } else {
            return this.fiscoBcos2Map.get(groupId).getEvent(eventId);
        }
    }

    public SendResult publishEvent(String topicName, Long groupId, String eventContent, String extensions) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.publishEvent(topicName, eventContent, extensions);
        } else {
            return this.fiscoBcos2Map.get(groupId).publishEvent(topicName, eventContent, extensions);
        }
    }

    public Long getBlockHeight(Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.getBlockHeight();
        } else {
            return this.fiscoBcos2Map.get(groupId).getBlockHeight();
        }
    }

    private String getRedisKey(Long blockNum, Long groupId) {
        if (this.fiscoBcos != null) {
            return Long.toString(blockNum);
        } else {
            return Long.toString(groupId) + blockNum;
        }
    }

    /**
     * get data from block chain and it's cache
     *
     * @param blockNum block height
     * @param groupId group id
     * @return list of WeEvent
     * @throws BrokerException BrokerException
     */
    public List<WeEvent> loop(Long blockNum, Long groupId) throws BrokerException {
        checkVersion(groupId);

        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        // try to get data from local cache and redis
        String key = getRedisKey(blockNum, groupId);
        try {
            if (blockCache != null && blockCache.containsKey(key)) {
                return blockCache.get(key);
            }
            if (redisService != null && redisService.isEventsExistInRedis(key)) {
                events = redisService.readEventsFromRedis(key);
                // redis data may be dirty
                if (events != null && !events.isEmpty()) {
                    return events;
                }
            }
        } catch (Exception e) {
            log.error("Exception happened while read events from redis server", e);
        }

        // from block chain
        if (this.fiscoBcos != null) {
            events = this.fiscoBcos.loop(blockNum);
        } else {
            events = this.fiscoBcos2Map.get(groupId).loop(blockNum);
        }

        //write events list to redis server
        try {
            if (events != null && !events.isEmpty()) {
                if (blockCache != null) {
                    blockCache.putIfAbsent(key, events);
                }
                if (redisService != null) {
                    redisService.writeEventsToRedis(key, events);
                }
            }
        } catch (Exception e) {
            log.error("Exception happened while write events to redis server", e);
        }

        return events;
    }
}
