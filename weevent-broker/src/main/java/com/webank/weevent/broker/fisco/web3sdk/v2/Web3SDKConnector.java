package com.webank.weevent.broker.fisco.web3sdk.v2;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupList;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Initialize web3j, connector to FISCO-BCOS.
 *
 * @author matthewliu
 * @since 2019/12/26
 */
@Slf4j
public class Web3SDKConnector {
    /**
     * The prefix of FISCO-BCOS version 2.X
     */
    public static final String FISCO_BCOS_2_X_VERSION_PREFIX = "2.";

    /**
     * default group in FISCO-BCOS, already exist
     */
    public static final String DEFAULT_GROUP_ID = "1";

    private Web3SDKConnector() {
    }

    /**
     * initialize web3j handler with given Service
     *
     * @param service web3sdk's service, haven't run
     * @return Web3j
     */
    public static Web3j initWeb3j(Service service) throws BrokerException {
        // init web3j with given group id
        try {
            log.info("begin to initialize web3sdk's Web3j, group id: {}", service.getGroupId());

            // special thread for TransactionSucCallback.onResponse, callback from IO thread directly if not setting
            //service.setThreadPool(poolTaskExecutor);
            service.run();

            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setChannelService(service);
            channelEthereumService.setTimeout(service.getConnectSeconds() * 1000);
            Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());

            // check connect with getNodeVersion command
            String nodeVersion = web3j.getNodeVersion().send().getNodeVersion().getVersion();
            if (StringUtils.isBlank(nodeVersion)
                    || !nodeVersion.contains(FISCO_BCOS_2_X_VERSION_PREFIX)) {
                log.error("init web3sdk failed, mismatch FISCO-BCOS version in node: {}", nodeVersion);
                throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
            }

            log.info("initialize web3sdk success, group id: {}", service.getGroupId());
            return web3j;
        } catch (Exception e) {
            log.error("init web3sdk failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    /**
     * initialize Service handler with given group id
     *
     * @return Service handler
     * @throws BrokerException BrokerException
     */
    public static Service initService(Long groupId, FiscoConfig fiscoConfig) throws BrokerException {
        log.info("begin to initialize web3sdk's Service, group id: {}", groupId);

        int web3sdkTimeout = fiscoConfig.getWeb3sdkTimeout();

        Service service = new Service();
        // group info
        service.setOrgID(fiscoConfig.getOrgId());
        service.setGroupId(groupId.intValue());
        service.setConnectSeconds(web3sdkTimeout / 1000);
        // reconnect idle time 100ms
        service.setConnectSleepPerMillis(100);

        // connect key and string
        GroupChannelConnectionsConfig connectionsConfig = new GroupChannelConnectionsConfig();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        connectionsConfig.setCaCert(resolver.getResource("classpath:" + fiscoConfig.getV2CaCrtPath()));
        connectionsConfig.setSslCert(resolver.getResource("classpath:" + fiscoConfig.getV2NodeCrtPath()));
        connectionsConfig.setSslKey(resolver.getResource("classpath:" + fiscoConfig.getV2NodeKeyPath()));

        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setGroupId(groupId.intValue());
        channelConnections.setConnectionsStr(Arrays.asList(fiscoConfig.getNodes().split(";")));
        connectionsConfig.setAllChannelConnections(Arrays.asList(channelConnections));

        service.setAllChannelConnections(connectionsConfig);
        return service;
    }

    public static ThreadPoolTaskExecutor initThreadPool(FiscoConfig fiscoConfig) {
        // init thread pool
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("web3sdk-");
        pool.setCorePoolSize(fiscoConfig.getWeb3sdkCorePoolSize());
        pool.setMaxPoolSize(fiscoConfig.getWeb3sdkMaxPoolSize());
        // queue conflict with thread pool scale up, forbid it
        pool.setQueueCapacity(0);
        pool.setKeepAliveSeconds(fiscoConfig.getWeb3sdkKeepAliveSeconds());
        // abort policy
        pool.setRejectedExecutionHandler(null);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init ThreadPoolTaskExecutor");
        return pool;
    }

    /**
     * get account Credentials
     *
     * @return Credentials return null if error
     */
    public static Credentials getCredentials(FiscoConfig fiscoConfig) {
        log.debug("begin init Credentials");

        Credentials credentials = GenCredential.create(fiscoConfig.getAccount());
        if (null == credentials) {
            log.error("init Credentials failed");
            return null;
        }

        log.info("init Credentials success");
        return credentials;
    }

    public static List<String> listGroupId(Web3j web3j, int timeout) throws BrokerException {
        try {
            GroupList groupList = web3j.getGroupList().sendAsync().get(timeout, TimeUnit.MILLISECONDS);
            return groupList.getGroupList();
        } catch (ExecutionException | InterruptedException e) {
            log.error("web3sdk execute failed", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException e) {
            log.error("web3sdk execute timeout", e);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }
}
