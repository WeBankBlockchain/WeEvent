package com.webank.weevent.broker.fisco.web3sdk;


import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.webank.weevent.broker.fisco.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.FiscoBcosConstants;
import com.webank.weevent.broker.fisco.contract.v2.Topic;
import com.webank.weevent.broker.fisco.contract.v2.TopicController;
import com.webank.weevent.broker.fisco.contract.v2.TopicData;
import com.webank.weevent.broker.util.DataTypeUtils;
import com.webank.weevent.broker.util.WeEventConstants;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.crud.CRUDService;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.precompile.exception.PrecompileMessageException;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterNumber;
import org.fisco.bcos.web3j.protocol.core.JsonRpc2_0Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupList;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Wrapper of Web3SDK 2.x function.
 * This class can run without spring's ApplicationContext.
 *
 * @author matthewliu
 * @since 2019/04/22
 */
@Slf4j
public class Web3SDK2Wrapper {
    // topic control address in CRUD
    public final static String WeEventTable = "WeEvent";
    public final static String WeEventTableKey = "key";
    public final static String WeEventTableValue = "value";
    public final static String WeEventTopicControlAddress = "topic_control_address";

    // it's a trick. Topic.getLogWeEventEvents is not static
    private static Topic topic;

    // static gas provider
    public static final ContractGasProvider gasProvider = new ContractGasProvider() {
        @Override
        public BigInteger getGasPrice(String contractFunc) {
            return FiscoBcosConstants.GAS_PRICE;
        }

        @Override
        @Deprecated
        public BigInteger getGasPrice() {
            return FiscoBcosConstants.GAS_PRICE;
        }

        @Override
        public BigInteger getGasLimit(String contractFunc) {
            return FiscoBcosConstants.GAS_LIMIT;
        }

        @Override
        @Deprecated
        public BigInteger getGasLimit() {
            return FiscoBcosConstants.GAS_LIMIT;
        }
    };

    /**
     * init web3j handler with given group id
     *
     * @param groupId group id
     * @return Web3j
     */
    public static Web3j initWeb3j(Long groupId, FiscoConfig fiscoConfig, ThreadPoolTaskExecutor poolTaskExecutor) throws BrokerException {
        // init web3j with given group id
        try {
            log.info("begin to initialize web3sdk, group id: {}", groupId);

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
            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setGroupId(groupId.intValue());
            channelConnections.setCaCertPath("classpath:" + fiscoConfig.getV2CaCrtPath());
            channelConnections.setNodeCaPath("classpath:" + fiscoConfig.getV2NodeCrtPath());
            channelConnections.setNodeKeyPath("classpath:" + fiscoConfig.getV2NodeKeyPath());
            channelConnections.setConnectionsStr(Arrays.asList(fiscoConfig.getNodes().split(";")));
            connectionsConfig.setAllChannelConnections(Arrays.asList(channelConnections));
            service.setAllChannelConnections(connectionsConfig);
            service.setThreadPool(poolTaskExecutor);
            service.run();

            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setChannelService(service);
            channelEthereumService.setTimeout(web3sdkTimeout);
            Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());

            // check connect with getNodeVersion command
            String nodeVersion = web3j.getNodeVersion().send().getNodeVersion().getVersion();
            if (StringUtils.isBlank(nodeVersion)
                    || !nodeVersion.contains(WeEventConstants.FISCO_BCOS_2_X_VERSION_PREFIX)) {
                log.error("init web3sdk failed, dismatch fisco version in node: {}", nodeVersion);
                throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
            }

            log.info("initialize web3sdk success, group id: {}", groupId);
            return web3j;
        } catch (Exception e) {
            log.error("init web3sdk failed, group id: " + groupId, e);
            throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
        }
    }

    public static List<String> listGroupId(Web3j web3j) throws BrokerException {
        try {
            GroupList groupList = web3j.getGroupList().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            return groupList.getGroupList();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            log.error("web3sdk execute failed", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * Table in CRUD, it's a key-value store.
     * WeEvent -> key, value
     * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-2.0/docs/manual/console.html#desc
     *
     * @param crud table service
     * @return opened or exist table
     */
    public static Table ensureTable(CRUDService crud) {
        try {
            return crud.desc(WeEventTable);
        } catch (PrecompileMessageException e) {
            log.info("not exist table in CRUD, create it: {}", WeEventTable);

            Table table = new Table(WeEventTable, WeEventTableKey, WeEventTableValue);
            try {
                int result = crud.createTable(table);
                if (result == 0) {
                    log.info("create table in CRUD success, {}", WeEventTable);
                    return table;
                }

                log.error("create table in CRUD failed, " + WeEventTable);
                return null;
            } catch (Exception e1) {
                log.error("create table in CRUD failed, " + WeEventTable, e1);
                return null;
            }
        } catch (Exception e) {
            log.error("ensure table in CRUD failed, " + WeEventTable, e);
            return null;
        }
    }

    /**
     * get address from CRUD table
     * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-2.0/en/docs/sdk/sdk.html?highlight=CRUDService#web3sdk-api
     *
     * @param web3j web3j
     * @param credentials credentials
     * @return address
     */
    public static String getAddress(Web3j web3j, Credentials credentials) throws BrokerException {
        String groupId = String.valueOf(((JsonRpc2_0Web3j) web3j).getGroupId());
        log.info("get topic control address from CRUD, groupId: {}", groupId);

        CRUDService crud = new CRUDService(web3j, credentials);
        Table table = ensureTable(crud);
        if (table == null) {
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }

        try {
            table.setKey(WeEventTopicControlAddress);
            Condition condition = table.getCondition();
            List<Map<String, String>> records = crud.select(table, condition);
            if (records.isEmpty()) {
                log.info("no record in CRUD table, {}", WeEventTable);
                return "";
            }
            if (records.size() != 1) {
                log.warn("more then one record in CRUD table, {}", WeEventTable);
            }
            return records.get(0).get(WeEventTableValue);
        } catch (Exception e) {
            log.error("select from CRUD table failed", e);
            return "";
        }
    }

    public static boolean addAddress(Web3j web3j, Credentials credentials, String address) throws BrokerException {
        String groupId = String.valueOf(((JsonRpc2_0Web3j) web3j).getGroupId());
        log.info("add topic control address into CRUD, groupId: {}", groupId);

        // check exist manually to avoid duplicate record
        String original = getAddress(web3j, credentials);
        if (!StringUtils.isBlank(original)) {
            log.info("topic control address already exist, {}", original);
            return false;
        }

        CRUDService crud = new CRUDService(web3j, credentials);
        Table table = ensureTable(crud);
        if (table == null) {
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }

        try {
            table.setKey(WeEventTopicControlAddress);
            org.fisco.bcos.web3j.precompile.crud.Entry record = table.getEntry();
            record.put(WeEventTableValue, address);
            // notice: record's key can be duplicate in CRUD
            int result = crud.insert(table, record);
            if (result == 1) {
                log.info("add topic control address into CRUD success");
                return true;
            }

            log.error("add topic control address into CRUD failed, {}", result);
            return false;
        } catch (Exception e) {
            log.error("add topic control address into CRUD failed", e);
            return false;
        }
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

    /**
     * load contract handler
     *
     * @param contractAddress contractAddress
     * @param web3j web3j
     * @param credentials credentials
     * @param cls contract java class
     * @return Contract return null if error
     */
    public static Contract loadContract(String contractAddress, Web3j web3j, Credentials credentials, Class<?> cls) {
        log.info("begin load contract, {}", cls.getSimpleName());

        try {
            // load contract
            Method method = cls.getMethod("load",
                    String.class,
                    Web3j.class,
                    Credentials.class,
                    BigInteger.class,
                    BigInteger.class);

            Object contract = method.invoke(null,
                    contractAddress,
                    web3j,
                    credentials,
                    FiscoBcosConstants.GAS_PRICE,
                    FiscoBcosConstants.GAS_LIMIT);

            if (contract == null) {
                log.info("load contract failed, {}", cls.getSimpleName());
                return null;
            } else {
                log.info("load contract success, {}", cls.getSimpleName());
                return (Contract) contract;
            }
        } catch (Exception e) {
            log.error("load contract failed, {} {}", cls.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * deploy topic control into web3j
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @return contract address
     * @throws BrokerException BrokerException
     */
    public static String deployTopicControl(Web3j web3j, Credentials credentials) throws BrokerException {
        log.info("begin deploy topic control");

        try {
            RemoteCall<TopicData> f1 = TopicData.deploy(web3j, credentials, gasProvider);
            TopicData topicData = f1.sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

            log.info("topic data contract address: {}", topicData.getContractAddress());
            if (topicData.getContractAddress().equals(FiscoBcosConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicData.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            RemoteCall<TopicController> f2 = TopicController.deploy(web3j, credentials, gasProvider, topicData.getContractAddress());
            TopicController topicController = f2.sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

            log.info("topic control contract address: {}", topicController.getContractAddress());
            if (topicController.getContractAddress().equals(FiscoBcosConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicController.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            log.info("deploy topic control success");
            return topicController.getContractAddress();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("deploy contract failed", e);
            throw new BrokerException("deploy contract failed");
        }
    }

    public static List<Topic.LogWeEventEventResponse> receipt2LogWeEventEventResponse(Web3j web3j, Credentials credentials, TransactionReceipt receipt) throws BrokerException {
        if (topic == null) {
            topic = Topic.load("0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", web3j, credentials, gasProvider);
        }

        return topic.getLogWeEventEvents(receipt);
    }

    /**
     * getBlockHeight
     *
     * @param web3j web3j
     * @return 0L if net error
     */
    public static Long getBlockHeight(Web3j web3j) throws BrokerException {
        try {
            BlockNumber blockNumber = web3j.getBlockNumber().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            // Web3sdk's rpc return null in "get".
            if (blockNumber == null) {
                return 0L;
            }
            Long blockHeight = blockNumber.getBlockNumber().longValue();
            log.debug("current block height: {}", blockHeight);
            return blockHeight;
        } catch (InterruptedException | ExecutionException | TimeoutException | RuntimeException e) {
            log.error("get block height failed due to InterruptedException|ExecutionException|TimeoutException|RuntimeException", e);
            throw new BrokerException(ErrorCode.GET_BLOCK_HEIGHT_ERROR);
        }
    }

    /**
     * Fetch all event in target block.
     *
     * @param blockNum the blockNum
     * @return null if net error
     */
    public static List<WeEvent> loop(Web3j web3j, Credentials credentials, Long blockNum) throws BrokerException {
        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        try {
            log.debug("fetch block, blockNum: {}", blockNum);

            // "false" to load only tx hash.
            BcosBlock bcosBlock = web3j.getBlockByNumber(new DefaultBlockParameterNumber(blockNum), false)
                    .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            List<String> transactionHashList = bcosBlock.getBlock().getTransactions().stream()
                    .map(transactionResult -> (String) transactionResult.get()).collect(Collectors.toList());
            if (transactionHashList.size() <= 0) {
                return events;
            }
            log.debug("tx in block: {}", transactionHashList.size());

            for (String transactionHash : transactionHashList) {
                BcosTransactionReceipt transactionReceipt = web3j.getTransactionReceipt(transactionHash)
                        .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                if (!transactionReceipt.getTransactionReceipt().isPresent()) {
                    log.error(String.format("loop block empty tx receipt, blockNum: %s tx hash: %s", blockNum, transactionHash));
                    return null;
                }

                TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();
                List<Topic.LogWeEventEventResponse> logWeEventEvents = Web3SDK2Wrapper.receipt2LogWeEventEventResponse(web3j, credentials, receipt);
                for (Topic.LogWeEventEventResponse logEvent : logWeEventEvents) {
                    WeEvent event = new WeEvent(logEvent.topicName,
                            logEvent.eventContent.getBytes(StandardCharsets.UTF_8),
                            DataTypeUtils.json2Map(logEvent.extensions));
                    event.setEventId(DataTypeUtils.encodeEventId(logEvent.topicName,
                            logEvent.eventBlockNumer.intValue(),
                            logEvent.eventSeq.intValue()));

                    log.debug("get a event from block chain: {}", event);
                    events.add(event);
                }
            }

            return events;
        } catch (ExecutionException | TimeoutException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("loop block failed due to ExecutionException|TimeoutException|NullPointerException|InterruptedException", e);
            return null;
        } catch (RuntimeException e) {
            log.error("loop block failed due to RuntimeException", e);
            throw new BrokerException("loop block failed due to RuntimeException", e);
        }
    }
}

