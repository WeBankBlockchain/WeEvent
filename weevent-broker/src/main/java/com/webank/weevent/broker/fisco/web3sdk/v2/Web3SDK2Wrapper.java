package com.webank.weevent.broker.fisco.web3sdk.v2;


import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import org.fisco.bcos.web3j.protocol.Web3jService;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterNumber;
import org.fisco.bcos.web3j.protocol.core.JsonRpc2_0Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupList;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeIDList;
import org.fisco.bcos.web3j.protocol.core.methods.response.TotalTransactionCount;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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
    public final static String WeEventTableVersion = "version";
    public final static String WeEventTopicControlAddress = "topic_control_address";
    // partial key of FISCO block info
    public final static String BLOCK_NUMBER = "blockNumber";
    public final static String NODE_ID = "nodeId";
    public final static String PEERS = "peers";
    public final static String VIEW = "view";


    // static gas provider
    public static final ContractGasProvider gasProvider = new ContractGasProvider() {
        @Override
        public BigInteger getGasPrice(String contractFunc) {
            return WeEventConstants.GAS_PRICE;
        }

        @Override
        @Deprecated
        public BigInteger getGasPrice() {
            return WeEventConstants.GAS_PRICE;
        }

        @Override
        public BigInteger getGasLimit(String contractFunc) {
            return WeEventConstants.GAS_LIMIT;
        }

        @Override
        @Deprecated
        public BigInteger getGasLimit() {
            return WeEventConstants.GAS_LIMIT;
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
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            connectionsConfig.setCaCert(resolver.getResource("classpath:" + fiscoConfig.getV2CaCrtPath()));
            connectionsConfig.setSslCert(resolver.getResource("classpath:" + fiscoConfig.getV2NodeCrtPath()));
            connectionsConfig.setSslKey(resolver.getResource("classpath:" + fiscoConfig.getV2NodeKeyPath()));

            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setGroupId(groupId.intValue());
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
                log.error("init web3sdk failed, mismatch FISCO-BCOS version in node: {}", nodeVersion);
                throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
            }

            log.info("initialize web3sdk success, group id: {}", groupId);
            return web3j;
        } catch (Exception e) {
            log.error("init web3sdk failed, group id: " + groupId, e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    public static void setBlockNotifyCallBack(Web3j web3j, FiscoBcosDelegate.IBlockEventListener listener) {
        Web3jService web3jService = ((JsonRpc2_0Web3j) web3j).web3jService();
        ((ChannelEthereumService) web3jService).getChannelService().setBlockNotifyCallBack(
                (int groupID, BigInteger blockNumber) -> listener.onEvent((long) groupID, blockNumber.longValue())
        );
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
    private static Table ensureTable(CRUDService crud) throws BrokerException {
        try {
            Table table = crud.desc(WeEventTable);
            if (WeEventTableKey.equals(table.getKey())) {
                List<String> fields = Arrays.asList(table.getValueFields().split(","));
                if (fields.size() == 2 && fields.contains(WeEventTableValue) && fields.contains(WeEventTableVersion)) {
                    return table;
                }
            }

            log.error("miss fields in CRUD table, {}/{}/{}", WeEventTableKey, WeEventTableValue, WeEventTableVersion);
            throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        } catch (PrecompileMessageException e) {
            log.info("not exist table in CRUD, create it: {}", WeEventTable);

            return createTable(crud);
        } catch (BrokerException e) {
            throw e;
        } catch (Exception e) {
            log.error("ensure table in CRUD failed, " + WeEventTable, e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    private static Table createTable(CRUDService crud) throws BrokerException {
        Table table = new Table(WeEventTable, WeEventTableKey, WeEventTableValue + "," + WeEventTableVersion);
        try {
            int result = crud.createTable(table);
            if (result == 0) {
                log.info("create table in CRUD success, {}", WeEventTable);
                return table;
            }

            log.error("create table in CRUD failed, " + WeEventTable);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (Exception e) {
            log.error("create table in CRUD failed, " + WeEventTable, e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    /**
     * list all address from CRUD table
     * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-2.0/en/docs/sdk/sdk.html?highlight=CRUDService#web3sdk-api
     *
     * @param web3j web3j
     * @param credentials credentials
     * @return address list
     */
    public static Map<Long, String> listAddress(Web3j web3j, Credentials credentials) throws BrokerException {
        String groupId = String.valueOf(((JsonRpc2_0Web3j) web3j).getGroupId());
        log.info("get topic control address from CRUD, groupId: {}", groupId);

        CRUDService crud = new CRUDService(web3j, credentials);
        Table table = ensureTable(crud);
        List<Map<String, String>> records;
        try {
            table.setKey(WeEventTopicControlAddress);
            Condition condition = table.getCondition();
            records = crud.select(table, condition);
            log.info("records in CRUD, groupId: {}, {}", groupId, records);
        } catch (Exception e) {
            log.error("select from CRUD table failed", e);
            throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }

        Map<Long, String> addresses = new HashMap<>();
        for (Map<String, String> record : records) {
            addresses.put(Long.valueOf(record.get(WeEventTableVersion)), record.get(WeEventTableValue));
        }
        return addresses;
    }

    public static boolean addAddress(Web3j web3j, Credentials credentials, Long version, String address) throws BrokerException {
        String groupId = String.valueOf(((JsonRpc2_0Web3j) web3j).getGroupId());
        log.info("add topic control address into CRUD, groupId: {}", groupId);

        // check exist manually to avoid duplicate record
        Map<Long, String> topicControlAddresses = listAddress(web3j, credentials);
        if (topicControlAddresses.containsKey(version)) {
            log.info("already exist in CRUD, {} {}", version, address);
            return false;
        }

        CRUDService crud = new CRUDService(web3j, credentials);
        Table table = ensureTable(crud);
        try {
            table.setKey(WeEventTopicControlAddress);
            org.fisco.bcos.web3j.precompile.crud.Entry record = table.getEntry();
            record.put(WeEventTableValue, address);
            record.put(WeEventTableVersion, String.valueOf(version));
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
    public static Contract loadContract(String contractAddress, Web3j web3j, Credentials credentials, Class<?> cls) throws BrokerException {
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
                    WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT);

            if (contract != null) {
                log.info("load contract success, {}", cls.getSimpleName());
                return (Contract) contract;
            }

            log.info("load contract failed, {}", cls.getSimpleName());
        } catch (Exception e) {
            log.error(String.format("load contract[%s] failed", cls.getSimpleName()), e);
        }

        throw new BrokerException(ErrorCode.LOAD_CONTRACT_ERROR);
    }

    /**
     * deploy topic control into web3j in Web3SDK2Wrapper.nowVersion
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @return contract address
     * @throws BrokerException BrokerException
     */
    public static String deployTopicControl(Web3j web3j, Credentials credentials) throws BrokerException {
        log.info("begin deploy topic control");

        try {
            // deploy Topic.sol in highest version(Web3SDK2Wrapper.nowVersion)
            RemoteCall<com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic> f1 = com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic.deploy(web3j, credentials, gasProvider);
            com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic topic = f1.sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            log.info("topic contract address: {}", topic.getContractAddress());
            if (topic.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after Topic.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            // deploy TopicController.sol in nowVersion
            RemoteCall<com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController> f2 = com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController.deploy(web3j, credentials, gasProvider, topic.getContractAddress());
            com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController topicController = f2.sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            log.info("topic control contract address: {}", topicController.getContractAddress());
            if (topicController.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicController.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            log.info("deploy topic control success");
            return topicController.getContractAddress();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("deploy contract failed", e);
            throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
        }
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
     * @param web3j the web3j
     * @param blockNum the blockNum
     * @param supportedVersion version list
     * @param historyTopic topic list
     * @return null if net error
     */
    public static List<WeEvent> loop(Web3j web3j, Long blockNum,
                                     Map<String, Long> supportedVersion,
                                     Map<String, Contract> historyTopic) throws BrokerException {
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
            if (transactionHashList.isEmpty()) {
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
                // tx.to is contract address
                String address = receipt.getTo();
                if (historyTopic.containsKey(address)) {
                    Long version = supportedVersion.get(address);
                    log.debug("detect event in version: {}", version);

                    WeEvent event = SupportedVersion.decodeWeEvent(receipt, version.intValue(), historyTopic);
                    if (event != null) {
                        log.debug("get a event from block chain: {}", event);
                        events.add(event);
                    }
                }
            }

            return events;
        } catch (TimeoutException e) {
            log.error("loop block failed due to timeout", e);
            return null;
        } catch (ExecutionException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("loop block failed due to web3sdk rpc error", e);
            return null;
        } catch (RuntimeException e) {
            log.error("loop block failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    public static GroupGeneral getGroupGeneral(Web3j web3j) throws BrokerException {
        // Current number of nodes, number of blocks, number of transactions
        GroupGeneral groupGeneral = new GroupGeneral();
        try {
            TotalTransactionCount totalTransactionCount = web3j.getTotalTransactionCount()
                    .sendAsync().get();
            TotalTransactionCount.TransactionCount transactionCount = totalTransactionCount.getTotalTransactionCount();
            BigInteger blockNumber = transactionCount.getBlockNumber();
            BigInteger txSum = transactionCount.getTxSum();

            NodeIDList nodeIDList = web3j.getNodeIDList().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            List<String> nodeIds = nodeIDList.getNodeIDList();

            groupGeneral.setNodeCount(nodeIds.size());
            groupGeneral.setLatestBlock(blockNumber);
            groupGeneral.setTransactionCount(txSum);
            return groupGeneral;
        } catch (ExecutionException | TimeoutException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("get group general failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        } catch (RuntimeException e) {
            log.error("get group general failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    //Traversing transactions
    public static ListPage<TbTransHash> queryTransList(Web3j web3j, String blockHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        ListPage<TbTransHash> tbTransHashListPage = new ListPage<>();
        List<TbTransHash> tbTransHashes = new ArrayList<>();

        try {
            if (blockHash != null) {
                // get TbTransHash list by blockHash
                BcosBlock bcosBlock = web3j.getBlockByHash(blockHash, true)
                        .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

                generateTbTransHashListPage(pageIndex, pageSize, tbTransHashListPage, tbTransHashes, bcosBlock);
            } else {
                // get TbTransHash list by blockNumber
                BigInteger blockNum = blockNumber;
                if (blockNumber == null) {
                    blockNum = web3j.getBlockNumber().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).getBlockNumber();
                }
                BcosBlock bcosBlock = web3j.getBlockByNumber(DefaultBlockParameter.valueOf(blockNum), true)
                        .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

                generateTbTransHashListPage(pageIndex, pageSize, tbTransHashListPage, tbTransHashes, bcosBlock);
            }
            tbTransHashListPage.setPageIndex(pageIndex);
            tbTransHashListPage.setPageSize(pageSize);
            return tbTransHashListPage;
        } catch (ExecutionException | TimeoutException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("query transaction failed due to web3sdk rpc error", e);
            throw new BrokerException("query transaction failed due to RuntimeException", e);
        } catch (RuntimeException e) {
            log.error("query transaction failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    private static void generateTbTransHashListPage(Integer pageIndex, Integer pageSize, ListPage<TbTransHash> tbTransHashListPage, List<TbTransHash> tbTransHashes, BcosBlock bcosBlock) throws BrokerException {
        BcosBlock.Block block = bcosBlock.getBlock();
        if (block == null || CollectionUtils.isEmpty(block.getTransactions())) {
            log.error("query transaction from block failed. transaction in block is empty");
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }

        Integer transCount = block.getTransactions().size();

        if (pageIndex < 1 || (pageIndex - 1) * pageSize > transCount) {
            log.error("pageIndex error.");
            throw new BrokerException("pageIndex error.");
        }
        Integer transSize = (transCount <= pageIndex * pageSize) ? (transCount - ((pageIndex - 1) * pageSize)) : pageSize;
        Integer transIndexStart = (pageIndex - 1) * pageSize;

        List<Transaction> transactionHashList = block.getTransactions().stream()
                .map(transactionResult -> (Transaction) transactionResult.get()).collect(Collectors.toList()).subList(transIndexStart, transSize + transIndexStart);
        transactionHashList.forEach(tx -> {
            TbTransHash tbTransHash = new TbTransHash(tx.getHash(), tx.getFrom(), tx.getTo(),
                    tx.getBlockNumber(), DataTypeUtils.getTimestamp(bcosBlock.getBlock().getTimestamp().longValue()));
            tbTransHashes.add(tbTransHash);
        });
        tbTransHashListPage.setPageSize(transSize);
        tbTransHashListPage.setTotal(transCount);
        tbTransHashListPage.setPageData(tbTransHashes);
    }

    //Traverse block
    public static ListPage<TbBlock> queryBlockList(Web3j web3j, String blockHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        ListPage<TbBlock> tbBlockListPage = new ListPage<>();
        List<TbBlock> tbBlocks = new CopyOnWriteArrayList<>();
        Integer blcokCount = 0;
        try {
            BcosBlock.Block block;
            if (blockHash != null) {
                BcosBlock bcosBlock = web3j.getBlockByHash(blockHash, true)
                        .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                block = bcosBlock.getBlock();
                blcokCount = 1;
                getTbBlockList(tbBlocks, block);
            } else if (blockNumber != null) {
                BcosBlock bcosBlock = web3j.getBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true)
                        .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                block = bcosBlock.getBlock();
                blcokCount = 1;
                getTbBlockList(tbBlocks, block);
            } else {
                BigInteger blockNum = web3j.getBlockNumber().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).getBlockNumber();

                if (pageIndex < 1 || (pageIndex-1) * pageSize > blockNum.longValue()) {
                    log.error("pageIndex error.");
                    throw new BrokerException("pageIndex error.");
                }
                Integer blockSize = (blockNum.longValue() <= pageIndex * pageSize) ? (Integer.valueOf(blockNum.toString()) - ((pageIndex-1) * pageSize)) : pageSize;
                long blockNumberIndex = (pageIndex-1) * pageSize + 1;

                List<Long> blockNums = new ArrayList<>();
                for (int i = 0; i < blockSize; i++) {
                    blockNums.add(blockNumberIndex);
                    blockNumberIndex ++;
                }
                blcokCount = blockNum.intValue();
                tbBlocks = getTbBlock(web3j, blockNums);

                Collections.sort(tbBlocks, (arg0, arg1) -> arg1.getBlockNumber().compareTo(arg0.getBlockNumber()));
            }
            tbBlockListPage.setPageIndex(pageIndex);
            tbBlockListPage.setPageSize(pageSize);
            tbBlockListPage.setTotal(blcokCount);
            tbBlockListPage.setPageData(tbBlocks);
            return tbBlockListPage;
        } catch (ExecutionException | TimeoutException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("query transaction failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        } catch (RuntimeException e) {
            log.error("query transaction failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    private static void getTbBlockList(List<TbBlock> tbBlocks, BcosBlock.Block block) throws BrokerException {
        if (block == null) {
            log.error("query block failed, block is null.");
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }

        String blockTimestamp = DataTypeUtils.getTimestamp(block.getTimestamp().longValue());

        int transactions = 0;
        if (!block.getTransactions().isEmpty()) {
            transactions = block.getTransactions().size();
        }
        int sealerIndex = Integer.parseInt(block.getSealer().substring(2), 16);
        TbBlock tbBlock = new TbBlock(block.getHash(), block.getNumber(), blockTimestamp,
                transactions, sealerIndex);
        tbBlock.setSealer(block.getSealer());
        tbBlocks.add(tbBlock);
    }

    public static synchronized ListPage<TbNode> queryNodeList(Web3j web3j) throws BrokerException {
        ListPage<TbNode> tbNodeListPage = new ListPage<>();
        //1、Current node, pbftview, and blockNumber
        List<TbNode> tbNodes = new ArrayList<>();
        try {

            List<String> observerList = web3j.getObserverList().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).getObserverList();
            List<String> sealerList = web3j.getSealerList().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).getSealerList();

            if (CollectionUtils.isEmpty(sealerList)) {
                log.error("nodeList query from web3j is empty.");
                throw new BrokerException("nodeList query from web3j is empty");
            }

            List<String> nodeIds = web3j.getNodeIDList()
                    .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).getNodeIDList();

            // get PbftView from each nodes
            Map<String, Map<String, String>> nodeViews = getNodeViews(web3j);
            // get blockNum from each nodes
            Map<String, Map<String, String>> nodeBlockNums = getBlockNums(web3j);

            for (String sealerNodeId : sealerList) {
                TbNode tbNode = generateTbNode(nodeViews, nodeBlockNums, sealerNodeId, nodeIds);
                tbNode.setNodeType(WeEventConstants.NODE_TYPE_SEALER);
                tbNodes.add(tbNode);
            }
            for (String observerNodeId : observerList) {
                TbNode tbNode = generateTbNode(nodeViews, nodeBlockNums, observerNodeId, nodeIds);
                tbNode.setNodeType(WeEventConstants.NODE_TYPE_OBSERVER);
                tbNodes.add(tbNode);
            }

            tbNodeListPage.setPageData(tbNodes);
            tbNodeListPage.setTotal(tbNodes.size());
            return tbNodeListPage;
        } catch (ExecutionException | TimeoutException | NullPointerException | InterruptedException | IOException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("query node failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        } catch (RuntimeException e) {
            log.error("query node failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    private static Map<String, Map<String, String>> getNodeViews(Web3j web3j) throws IOException {
        JSONArray consensusStatusArr = JSONObject.parseArray(web3j.getConsensusStatus().sendForReturnString());
        Map<String, Map<String, String>> nodeviews = new HashMap<>();
        for (int i =0; i < consensusStatusArr.size(); i++){
            Object jsonObj = consensusStatusArr.get(i);
            if (jsonObj instanceof JSONArray) {
                convertJSONArrayToList(nodeviews, (JSONArray) jsonObj);
            }
        }
        return nodeviews;
    }

    private static Map<String, Map<String, String>> getBlockNums(Web3j web3j) throws IOException {
        JSONObject syncStatusArr = JSONObject.parseObject(web3j.getSyncStatus().sendForReturnString());
        Map<String, Map<String, String>> nodeBlockNums = new HashMap<>();
        Set<String> set = syncStatusArr.keySet();
        Map<String, String> map = new HashMap<>();
        for (String s : set) {
            if (BLOCK_NUMBER.equals(s) || NODE_ID.equals(s)) {
                map.put(s, syncStatusArr.getString(s));
            }
            if (PEERS.equals(s)) {
                String peersListStr = syncStatusArr.getString(s);
                JSONArray peersListJSONArray = JSONObject.parseArray(peersListStr);
                convertJSONArrayToList(nodeBlockNums, peersListJSONArray);
            }
        }
        nodeBlockNums.put(syncStatusArr.getString(NODE_ID), map);
        return nodeBlockNums;
    }

    private static void convertJSONArrayToList(Map<String, Map<String, String>> map, JSONArray jsonObj) {
        for (int i = 0; i < jsonObj.size(); i++) {
            Object object = jsonObj.get(i);
            if (object instanceof JSONObject) {
                Set<String> keySet = ((JSONObject) object).keySet();
                Map<String, String> objMap = new HashMap<>();
                for (String key : keySet) {
                    objMap.put(key, ((JSONObject) object).getString(key));
                }
                map.put(objMap.get(NODE_ID), objMap);
            }
        }
    }

    private static TbNode generateTbNode(Map<String, Map<String, String>> nodeViews,
                                         Map<String, Map<String, String>> nodeBlockNums,
                                         String nodeId, List<String> nodeIds) {
        TbNode tbNode = new TbNode();
        BigInteger blockNum = null;
        BigInteger pbftView = null;
        if (nodeBlockNums.containsKey(nodeId)) {
            blockNum = new BigInteger(nodeBlockNums.get(nodeId).get(BLOCK_NUMBER));
        }
        if (nodeViews.containsKey(nodeId)) {
            pbftView = new BigInteger(nodeViews.get(nodeId).get(VIEW));
        }
        tbNode.setNodeId(nodeId);
        tbNode.setBlockNumber(blockNum);
        tbNode.setPbftView(pbftView);
        tbNode.setNodeActive(checkNodeActive(nodeId, nodeIds));
        return tbNode;
    }

    private static int checkNodeActive(String nodeId, List<String> nodeIds) {
        // 1 means node active; 0 means inactive
        return nodeIds.contains(nodeId)? 1 : 0;
    }

    private static CopyOnWriteArrayList<TbBlock> getTbBlock(Web3j web3j, List<Long> blockNums) throws ExecutionException, InterruptedException {

        CompletableFuture<List<TbBlock>>[] completableFutureArr = new CompletableFuture[blockNums.size()];
        CopyOnWriteArrayList<TbBlock> tbBlocks = new CopyOnWriteArrayList<>();
        for (int i = 0; i < blockNums.size(); i++) {
            long blockNumber = blockNums.get(i);
            CompletableFuture<List<TbBlock>> future = CompletableFuture.supplyAsync(() ->{
                BcosBlock bcosBlock = null;
                try {
                    bcosBlock = web3j.getBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true)
                            .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException |ExecutionException |TimeoutException e) {
                    log.error("query block by blockNumber failed. e:", e);
                    return null;
                }
                BcosBlock.Block block = bcosBlock.getBlock();

                if (block == null) {
                    return null;
                }

                String blockTimestamp = DataTypeUtils.getTimestamp(block.getTimestamp().longValue());

                int transactions = 0;
                if (!block.getTransactions().isEmpty()) {
                    transactions = block.getTransactions().size();
                }
                int sealerIndex = Integer.parseInt(block.getSealer().substring(2), 16);
                TbBlock tbBlock = new TbBlock(block.getHash(), block.getNumber(), blockTimestamp,
                        transactions, sealerIndex);
                tbBlock.setSealer(block.getSealer());
                tbBlocks.add(tbBlock);
                return tbBlocks;
            });

            completableFutureArr[i] = future;
        }

        CompletableFuture<Void> combindFuture = CompletableFuture.allOf(completableFutureArr);
        combindFuture.get();

        return tbBlocks;
    }
}

