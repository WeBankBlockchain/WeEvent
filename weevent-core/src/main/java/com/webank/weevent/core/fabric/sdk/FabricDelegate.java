package com.webank.weevent.core.fabric.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.core.config.FabricConfig;
import com.webank.weevent.core.fisco.web3sdk.Web3SDKConnector;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/23
 */
@Slf4j
public class FabricDelegate {
    private Map<String, Fabric> fabricMap;

    private static List<String> channels = new ArrayList<>();

    // fabricConfig
    private FabricConfig fabricConfig;

    // binding thread pool
    public ThreadPoolTaskExecutor threadPool;

    public FabricDelegate() {
        this.fabricMap = new ConcurrentHashMap<>();
    }

    public void initProxy(FabricConfig fabricConfig) throws BrokerException {
        this.fabricConfig = fabricConfig;
        this.threadPool = Web3SDKConnector.initThreadPool(fabricConfig.getCorePoolSize(),
                fabricConfig.getMaxPoolSize(),
                fabricConfig.getKeepAliveSeconds());

        Fabric fabric = new Fabric(fabricConfig);
        fabric.init(fabricConfig.getChannelName());
        fabricMap.put(fabricConfig.getChannelName(), fabric);
        channels = fabric.listChannelName(fabricConfig);


    }

    public ThreadPoolTaskExecutor getThreadPool() {
        return this.threadPool;
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

        return this.fabricMap.get(channelName).loop(blockNum);
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
}
