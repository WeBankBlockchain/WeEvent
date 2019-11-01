package com.webank.weevent.broker.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

/**
 * FISCO-BCOS Config that loaded by java, not spring.
 * Because Web3sdkUtils.main is a pure java process, no spring ApplicationContext.
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/1/28
 */
@Slf4j
@Data
@PropertySource(value = "classpath:fisco.properties", encoding = "UTF-8")
public class FiscoConfig {
    public final static String propertiesFileKey = "block-chain-properties";

    @Value("${version:}")
    private String version;

    @Value("${orgid:fisco}")
    private String orgId;

    @Value("${nodes:}")
    private String nodes;

    @Value("${proxy.address:}")
    private String proxyAddress;

    @Value("${account:bcec428d5205abe0f0cc8a734083908d9eb8563e31f943d760786edf42ad67dd}")
    private String account;

    @Value("${web3sdk.timeout:10000}")
    private Integer web3sdkTimeout;

    @Value("${web3sdk.core-pool-size:10}")
    private Integer web3sdkCorePoolSize;

    @Value("${web3sdk.max-pool-size:1000}")
    private Integer web3sdkMaxPoolSize;

    @Value("${web3sdk.keep-alive-seconds:10}")
    private Integer web3sdkKeepAliveSeconds;

    @Value("${v1.ca-crt-path:ca.crt}")
    private String v1CaCrtPath;

    @Value("${v1.client-crt-password:123456}")
    private String v1ClientCrtPassword;

    @Value("${v1.client-key-store-path:client.keystore}")
    private String v1ClientKeyStorePath;

    @Value("${v1.key-store-password:123456}")
    private String v1KeyStorePassword;

    @Value("${v2.ca-crt-path:v2/ca.crt}")
    private String v2CaCrtPath;

    @Value("${v2.node-crt-path:v2/node.crt}")
    private String v2NodeCrtPath;

    @Value("${v2.node-key-path:v2/node.key}")
    private String v2NodeKeyPath;

    @Value("${consumer.idle-time:1000}")
    private Integer consumerIdleTime;

    @Value("${consumer.history_merge_block:8}")
    private Integer consumerHistoryMergeBlock;

    /**
     * load configuration without spring
     *
     * @return true if success, else false
     */
    public boolean load() {
        return new SmartLoadConfig().load(this, propertiesFileKey);
    }
}
