package com.webank.weevent.core.fisco.web3sdk.v2;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.core.config.FiscoConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupList;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.springframework.core.io.Resource;
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

    // The prefix of FISCO-BCOS version 2.X
    public static final String FISCO_BCOS_2_X_VERSION_PREFIX = "2.";

    // default group in FISCO-BCOS, already exist
    public static final String DEFAULT_GROUP_ID = "1";

    // FISCO-BCOS chain id
    public static String chainID;

    private Web3SDKConnector() {
    }

    /*
     * initialize web3j handler with given Service
     *
     * @param service web3sdk's service
     * @return Web3j
     */
    public static Web3j initWeb3j(Service service) throws BrokerException {
        // init web3j with given group id
        try {
            log.info("begin to initialize web3sdk's Web3j, group id: {}", service.getGroupId());
            StopWatch sw = StopWatch.createStarted();

            // special thread for TransactionSucCallback.onResponse, callback from IO thread directly if not setting
            //service.setThreadPool(poolTaskExecutor);
            service.run();

            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setChannelService(service);
            channelEthereumService.setTimeout(service.getConnectSeconds() * 1000);
            Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());

            // check connect with getNodeVersion command
            NodeVersion.Version version = web3j.getNodeVersion().send().getNodeVersion();
            String nodeVersion = version.getVersion();
            if (StringUtils.isBlank(nodeVersion)
                    || !nodeVersion.contains(FISCO_BCOS_2_X_VERSION_PREFIX)) {
                log.error("init web3sdk failed, mismatch FISCO-BCOS version in node: {}", nodeVersion);
                throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
            }
            chainID = version.getChainID();

            sw.stop();
            log.info("initialize web3sdk success, group id: {} cost: {} ms", service.getGroupId(), sw.getTime());
            return web3j;
        } catch (Exception e) {
            log.error("init web3sdk failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    /*
     * initialize Service handler with given group id
     * Notice: returned Service haven't run
     *
     * @param groupId group id
     * @param fiscoConfig fisco Config
     * @return Service handler
     * @throws BrokerException BrokerException
     */
    public static Service initService(Long groupId, FiscoConfig fiscoConfig) throws BrokerException {
        log.info("begin to initialize web3sdk's Service, group id: {}", groupId);

        try {
            int web3sdkTimeout = fiscoConfig.getWeb3sdkTimeout();

            Service service = new Service();
            // change jdk.tls.namedGroups will cause https's bug: ERR_SSL_VERSION_OR_CIPHER_MISMATCH
            service.setSetJavaOpt(false);
            // group info
            service.setOrgID(fiscoConfig.getOrgId());
            service.setGroupId(groupId.intValue());
            service.setConnectSeconds(web3sdkTimeout / 1000);
            // reconnect idle time 100ms
            service.setConnectSleepPerMillis(100);

            // connect key and string
            GroupChannelConnectionsConfig connectionsConfig = new GroupChannelConnectionsConfig();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            connectionsConfig.setCaCert(resolver.getResource("classpath:" + fiscoConfig.getCaCrtPath()));
            connectionsConfig.setSslCert(resolver.getResource("classpath:" + fiscoConfig.getSdkCrtPath()));
            connectionsConfig.setSslKey(resolver.getResource("classpath:" + fiscoConfig.getSdkKeyPath()));

            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setGroupId(groupId.intValue());
            channelConnections.setConnectionsStr(Arrays.asList(fiscoConfig.getNodes().split(";")));
            connectionsConfig.setAllChannelConnections(Collections.singletonList(channelConnections));

            service.setAllChannelConnections(connectionsConfig);
            return service;
        } catch (Exception e) {
            log.error("init web3sdk's Service failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_SERVICE_ERROR);
        }
    }

    public static ThreadPoolTaskExecutor initThreadPool(int core, int max, int keepalive) {
        // init thread pool
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("web3sdk-");
        pool.setCorePoolSize(core);
        pool.setMaxPoolSize(max);
        // queue conflict with thread pool scale up, forbid it
        pool.setQueueCapacity(0);
        pool.setKeepAliveSeconds(keepalive);
        // abort policy
        pool.setRejectedExecutionHandler(null);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init ThreadPoolTaskExecutor");
        return pool;
    }

    public static Credentials getCredentials(FiscoConfig fiscoConfig) {
        log.debug("begin init Credentials");

        // read OSSCA account
        String privateKey;
        if (fiscoConfig.getWeb3sdkEncryptType().equals("SM2_TYPE")) {
            log.info("SM2_TYPE");
            try {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource resource = resolver.getResource("classpath:" + fiscoConfig.getPemKeyPath());

                PEMManager pemManager = new PEMManager();
                pemManager.load(resource.getInputStream());
                ECKeyPair pemKeyPair = pemManager.getECKeyPair();
                privateKey = pemKeyPair.getPrivateKey().toString(16);
            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | CertificateException | IOException e) {
                log.error("Init OSSCA Credentials failed", e);
                return null;
            }
        } else {
            privateKey = fiscoConfig.getAccount();
        }

        Credentials credentials = GenCredential.create(privateKey);
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
