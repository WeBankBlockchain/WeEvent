package com.webank.weevent.core.fisco.web3sdk.v2;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.core.config.FiscoConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Initialize client, connector to FISCO-BCOS.
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

    /**
     * build BcosSDK
     *
     * @param fiscoConfig fisco properties
     * @return BcosSDK instance
     * @throws BrokerException ConfigException
     */
    public static BcosSDK buidBcosSDK(FiscoConfig fiscoConfig) throws BrokerException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ConfigOption configOption;

        try {
            Map<String, Object> cryptoMaterial = new HashMap<>();
            cryptoMaterial.put("caCert",
                    resolver.getResource("classpath:" + fiscoConfig.getCaCrtPath()).getFile().getPath());
            cryptoMaterial.put("sslKey",
                    resolver.getResource("classpath:" + fiscoConfig.getSdkKeyPath()).getFile().getPath());
            cryptoMaterial.put("sslCert",
                    resolver.getResource("classpath:" + fiscoConfig.getSdkCrtPath()).getFile().getPath());

            if (fiscoConfig.getWeb3sdkEncryptType().equals("SM2_TYPE")) {
                log.info("SM2_TYPE");
                cryptoMaterial.put("enSslKey",
                        resolver.getResource("classpath:" + fiscoConfig.getSdkGmKeyPath()).getFile().getPath());
                cryptoMaterial.put("enSslCert",
                        resolver.getResource("classpath:" + fiscoConfig.getSdkCrtPath()).getFile().getPath());
            }
            Map<String, Object> network = new HashMap<>();
            network.put("peers", Arrays.asList(fiscoConfig.getNodes().split(";")));

            Map<String, Object> account = new HashMap<>();
//            if (fiscoConfig.getWeb3sdkEncryptType().equals("SM2_TYPE")) {
//                account.put("accountAddress",
//                        resolver.getResource("classpath:" + fiscoConfig.getPemKeyPath()).getFile().getPath());
//            }
            Map<String, Object> threadPool = new HashMap<>();
            threadPool.put("maxBlockingQueueSize", String.valueOf(fiscoConfig.getMaxBlockingQueueSize()));

            ConfigProperty configProperty = new ConfigProperty();
            configProperty.setAccount(account);
            configProperty.setCryptoMaterial(cryptoMaterial);
            configProperty.setNetwork(network);
            configProperty.setThreadPool(threadPool);

            if (fiscoConfig.getWeb3sdkEncryptType().equals("ECDSA_TYPE")) {
                configOption = new ConfigOption(configProperty, CryptoType.ECDSA_TYPE);
            } else if (fiscoConfig.getWeb3sdkEncryptType().equals("SM2_TYPE")) {
                configOption = new ConfigOption(configProperty, CryptoType.SM_TYPE);
            } else {
                log.error("unknown encrypt type:{}, support ECDSA_TYPE or SM2_TYPE", fiscoConfig.getWeb3sdkEncryptType());
                throw new BrokerException(ErrorCode.BCOS_SDK_BUILD_ERROR);
            }
        } catch (ConfigException | IOException e) {
            log.error("build BcosSDK, load configOption fail", e);
            throw new BrokerException(ErrorCode.BCOS_SDK_BUILD_ERROR);
        }

        return new BcosSDK(configOption);
    }

    /*
     * initialize client handler with given BcosSDK
     *
     * @param sdk web3sdk's bcosSDK
     * @param groupId groupId
     * @return Client
     */
    public static Client initClient(BcosSDK sdk, Integer groupId, FiscoConfig fiscoConfig) throws BrokerException {
        // init client with given group id
        try {
            log.info("begin to initialize web3sdk's BcosSDK, group id: {}", groupId);
            StopWatch sw = StopWatch.createStarted();

            Client client = sdk.getClient(groupId);
            if (fiscoConfig.getWeb3sdkEncryptType().equals("ECDSA_TYPE")) {
                CryptoKeyPair keyPair = client.getCryptoSuite().getKeyPairFactory().createKeyPair(fiscoConfig.getAccount());
                client.getCryptoSuite().setCryptoKeyPair(keyPair);
            }

            String address = client.getCryptoSuite().getCryptoKeyPair().getAddress();
            String hexPrivateKey = client.getCryptoSuite().getCryptoKeyPair().getHexPrivateKey();
            System.out.println(client);

            // check connect with getNodeVersion command
            org.fisco.bcos.sdk.model.NodeVersion version = client.getNodeVersion();
            String nodeVersion = version.getNodeVersion().getVersion();
            if (StringUtils.isBlank(nodeVersion)
                    || !nodeVersion.contains(FISCO_BCOS_2_X_VERSION_PREFIX)) {
                log.error("init web3sdk failed, mismatch FISCO-BCOS version in node: {}", nodeVersion);
                throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
            }
            chainID = version.getNodeVersion().getChainId();

            sw.stop();
            log.info("initialize client success, group id: {} cost: {} ms", groupId, sw.getTime());
            return client;
        } catch (Exception e) {
            log.error("init client failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

//    /*
//     * initialize Service handler with given group id
//     * Notice: returned Service haven't run
//     *
//     * @param groupId group id
//     * @param fiscoConfig fisco Config
//     * @return Service handler
//     * @throws BrokerException BrokerException
//     */
//    public static Service initService(Long groupId, FiscoConfig fiscoConfig) throws BrokerException {
//        log.info("begin to initialize web3sdk's Service, group id: {}", groupId);
//
//        try {
//            int web3sdkTimeout = fiscoConfig.getWeb3sdkTimeout();
//
//            Service service = new Service();
//            // change jdk.tls.namedGroups will cause https's bug: ERR_SSL_VERSION_OR_CIPHER_MISMATCH
//            service.setSetJavaOpt(false);
//            // group info
//            service.setOrgID(fiscoConfig.getOrgId());
//            service.setGroupId(groupId.intValue());
//            service.setConnectSeconds(web3sdkTimeout / 1000);
//            // reconnect idle time 100ms
//            service.setConnectSleepPerMillis(100);
//
//            // connect key and string
//            GroupChannelConnectionsConfig connectionsConfig = new GroupChannelConnectionsConfig();
//            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//            connectionsConfig.setCaCert(resolver.getResource("classpath:" + fiscoConfig.getCaCrtPath()));
//            connectionsConfig.setSslCert(resolver.getResource("classpath:" + fiscoConfig.getSdkCrtPath()));
//            connectionsConfig.setSslKey(resolver.getResource("classpath:" + fiscoConfig.getSdkKeyPath()));
//
//            ChannelConnections channelConnections = new ChannelConnections();
//            channelConnections.setGroupId(groupId.intValue());
//            channelConnections.setConnectionsStr(Arrays.asList(fiscoConfig.getNodes().split(";")));
//            connectionsConfig.setAllChannelConnections(Collections.singletonList(channelConnections));
//
//            service.setAllChannelConnections(connectionsConfig);
//            return service;
//        } catch (Exception e) {
//            log.error("init web3sdk's Service failed", e);
//            throw new BrokerException(ErrorCode.WEB3SDK_INIT_SERVICE_ERROR);
//        }
//    }

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

//    public static Credentials getCredentials(FiscoConfig fiscoConfig) {
//        log.debug("begin init Credentials");
//
//        // read OSSCA account
//        String privateKey;
//        if (fiscoConfig.getWeb3sdkEncryptType().equals("SM2_TYPE")) {
//            log.info("SM2_TYPE");
//            try {
//                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//                Resource resource = resolver.getResource("classpath:" + fiscoConfig.getPemKeyPath());
//
//                PEMManager pemManager = new PEMManager();
//                pemManager.load(resource.getInputStream());
//                ECKeyPair pemKeyPair = pemManager.getECKeyPair();
//                privateKey = pemKeyPair.getPrivateKey().toString(16);
//            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | CertificateException | IOException e) {
//                log.error("Init OSSCA Credentials failed", e);
//                return null;
//            }
//        } else {
//            privateKey = fiscoConfig.getAccount();
//        }
//
//        Credentials credentials = GenCredential.create(privateKey);
//        if (null == credentials) {
//            log.error("init Credentials failed");
//            return null;
//        }
//
//        log.info("init Credentials success");
//        return credentials;
//    }

    public static List<String> listGroupId(Client client) {
        return client.getGroupList().getGroupList();
    }
}
