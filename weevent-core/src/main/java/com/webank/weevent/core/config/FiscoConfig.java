package com.webank.weevent.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.ToString;

/**
 * FISCO-BCOS Config that support loaded by spring context and pure java
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/1/28
 */
@Data
@ToString
@Component
@PropertySource(value = "classpath:fisco.properties", ignoreResourceNotFound = true, encoding = "UTF-8")
public class FiscoConfig {
	
    public final static String propertiesFileKey = "block-chain-properties";

    @Value("${version}")
    private String version;

    @Value("${orgid}")
    private String orgId;

    @Value("${nodes}")
    private String nodes;

    @Value("${account}")
    private String account;

    @Value("${web3sdk.timeout}")
    private Integer web3sdkTimeout;

    @Value("${web3sdk.core-pool-size}")
    private Integer web3sdkCorePoolSize;

    @Value("${web3sdk.max-pool-size}")
    private Integer web3sdkMaxPoolSize;

    @Value("${web3sdk.keep-alive-seconds}")
    private Integer web3sdkKeepAliveSeconds;

    @Value("${web3sdk.encrypt-type}")
    private String web3sdkEncryptType;

    @Value("${ca-crt-path}")
    private String CaCrtPath;

    @Value("${sdk-crt-path}")
    private String SdkCrtPath;

    @Value("${sdk-key-path}")
    private String SdkKeyPath;

    @Value("${sdk-gm-crt-path}")
    private String SdkGmCrtPath;

    @Value("${sdk-gm-key-path}")
    private String SdkGmKeyPath;

    @Value("${pem-key-path}")
    private String PemKeyPath;

    @Value("${consumer.idle-time}")
    private Integer consumerIdleTime;

    @Value("${consumer.history_merge_block}")
    private Integer consumerHistoryMergeBlock;

    @Value("${bcosSDK.max_blocking_queue_size}")
    private Integer maxBlockingQueueSize;
    
    /**
     * load configuration without spring
     *
     * @param configFile config file, if empty load from default location
     * @return true if success, else false
     */
    public boolean load(String configFile) {
        return new SmartLoadConfig().load(this, configFile, propertiesFileKey);
    }
    
}
