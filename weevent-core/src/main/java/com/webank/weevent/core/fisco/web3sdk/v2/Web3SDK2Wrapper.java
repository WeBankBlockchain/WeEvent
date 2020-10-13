package com.webank.weevent.core.fisco.web3sdk.v2;


import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.dto.GroupGeneral;
import com.webank.weevent.core.dto.ListPage;
import com.webank.weevent.core.dto.TbBlock;
import com.webank.weevent.core.dto.TbNode;
import com.webank.weevent.core.dto.TbTransHash;
import com.webank.weevent.core.fisco.constant.WeEventConstants;
import com.webank.weevent.core.fisco.util.DataTypeUtils;
import com.webank.weevent.core.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.core.fisco.web3sdk.v2.solc10.Topic;
import com.webank.weevent.core.fisco.web3sdk.v2.solc10.TopicController;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus;
import org.fisco.bcos.sdk.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.gas.ContractGasProvider;
import org.fisco.bcos.sdk.utils.Numeric;

/**
 * Wrapper of Web3SDK 2.x function.
 * This class can run without spring's ApplicationContext.
 *
 * @author matthewliu
 * @since 2019/04/22
 */
@Slf4j
public class Web3SDK2Wrapper {
    // partial key of FISCO block info
    public final static String BLOCK_NUMBER = "blockNumber";
    public final static String NODE_ID = "nodeId";
    public final static String PEERS = "peers";
    public final static String VIEW = "view";

    // static gas provider
    public static final ContractGasProvider gasProvider = new ContractGasProvider() {
        @Override
        public BigInteger getGasPrice() {
            return WeEventConstants.GAS_PRICE;
        }

        @Override
        public BigInteger getGasLimit() {
            return WeEventConstants.GAS_LIMIT;
        }
    };

    public static void setBlockNotifyCallBack(BcosSDK sdk, FiscoBcosDelegate.IBlockEventListener listener) {
        sdk.getGroupManagerService().registerBlockNotifyCallback((s, blockNumberNotification) -> listener.onEvent(
                Long.parseLong(blockNumberNotification.getGroupId()),
                Long.parseLong(blockNumberNotification.getBlockNumber())));
    }

    /*
     * load contract handler
     *
     * @param contractAddress contractAddress
     * @param client client
     * @param cls contract java class
     * @return Contract return null if error
     */
    public static Contract loadContract(String contractAddress, Client client, Class<?> cls) throws BrokerException {
        log.info("begin load contract, {}", cls.getSimpleName());

        try {
            // load contract
            Method method = cls.getMethod("load",
                    String.class,
                    Client.class,
                    CryptoKeyPair.class);

            Object contract = method.invoke(null,
                    contractAddress,
                    client,
                    client.getCryptoSuite().getCryptoKeyPair());

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

    /*
     * deploy topic control into web3j in Web3SDK2Wrapper.nowVersion
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @return contract address
     * @throws BrokerException BrokerException
     */
    public static String deployTopicControl(Client client) throws BrokerException {
        log.info("begin deploy topic control");

        try {
            // deploy Topic.sol in highest version(Web3SDK2Wrapper.nowVersion)
            Topic topic = Topic.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            log.info("topic contract address: {}", topic.getContractAddress());
            if (topic.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after Topic.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            // deploy TopicController.sol in nowVersion
            TopicController topicController = TopicController.deploy(
                    client,
                    client.getCryptoSuite().getCryptoKeyPair(),
                    topic.getContractAddress());
            log.info("topic control contract address: {}", topicController.getContractAddress());
            if (topicController.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicController.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            log.info("deploy topic control success");
            return topicController.getContractAddress();
        } catch (ContractException e) {
            log.error("deploy contract failed", e);
            throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
        }
    }

    /*
     * getBlockHeight
     *
     * @param web3j web3j
     * @return 0L if net error
     */
    public static Long getBlockHeight(Client client) throws BrokerException {
        try {
            BlockNumber blockNumber = client.getBlockNumber();
            // Web3sdk's rpc return null in "get".
            if (blockNumber == null) {
                return 0L;
            }
            Long blockHeight = blockNumber.getBlockNumber().longValue();
            log.debug("current block height: {}", blockHeight);
            return blockHeight;
        } catch (Exception e) {
            log.error("get block height failed due to InterruptedException|ExecutionException|TimeoutException", e);
            throw new BrokerException(ErrorCode.GET_BLOCK_HEIGHT_ERROR);
        }
    }

    /*
     * Fetch all event in target block.
     *
     * @param web3j the web3j
     * @param blockNum the blockNum
     * @param supportedVersion version list
     * @param historyTopic topic list
     * @return null if net error
     */
    public static List<WeEvent> loop(Client client, BigInteger blockNum,
                                     Map<String, Long> supportedVersion,
                                     Map<String, Contract> historyTopic) throws BrokerException {
        List<WeEvent> events = new ArrayList<>();
        if (blockNum.compareTo(BigInteger.ZERO) <= 0) {
            return events;
        }

        try {
            log.debug("fetch block, blockNum: {}", blockNum);

            // "false" to load only tx hash.
            BcosBlock bcosBlock = client.getBlockByNumber(blockNum, false);
            BigInteger timestamp = Numeric.decodeQuantity(bcosBlock.getBlock().getTimestamp());
            List<String> transactionHashList = bcosBlock.getBlock().getTransactions().stream()
                    .map(transactionResult -> (String) transactionResult.get()).collect(Collectors.toList());
            if (transactionHashList.isEmpty()) {
                return events;
            }
            log.debug("tx in block: {}", transactionHashList.size());

            for (String transactionHash : transactionHashList) {
                Optional<TransactionReceipt> transactionReceipt = client.getTransactionReceipt(transactionHash)
                        .getTransactionReceipt();
                if (!transactionReceipt.isPresent()) {
                    log.error(String.format("loop block empty tx receipt, blockNum: %s tx hash: %s", blockNum, transactionHash));
                    return null;
                }

                TransactionReceipt receipt = transactionReceipt.get();
                // tx.to is contract address
                String address = receipt.getTo();
                if (historyTopic.containsKey(address)) {
                    Long version = supportedVersion.get(address);
                    log.debug("detect event in version: {}", version);

                    WeEvent event = SupportedVersion.decodeWeEvent(timestamp, receipt, version.intValue(), historyTopic);
                    if (event != null) {
                        log.debug("get an event from block chain: {}", event);
                        events.add(event);
                    }
                }
            }

            return events;
        } catch (NullPointerException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("loop block failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    public static GroupGeneral getGroupGeneral(Client client) throws BrokerException {
        // Current number of nodes, number of blocks, number of transactions
        GroupGeneral groupGeneral = new GroupGeneral();
        try {
            TotalTransactionCount.TransactionCountInfo transactionCount = client.getTotalTransactionCount().getTotalTransactionCount();
            BigInteger blockNumber = Numeric.decodeQuantity(transactionCount.getBlockNumber());
            BigInteger txSum = Numeric.decodeQuantity(transactionCount.getTxSum());

            List<String> nodeIds = client.getNodeIDList().getNodeIDList();

            groupGeneral.setNodeCount(nodeIds.size());
            groupGeneral.setLatestBlock(blockNumber);
            groupGeneral.setTransactionCount(txSum);
            return groupGeneral;
        } catch (NullPointerException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("get group general failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    //Traversing transactions
    public static ListPage<TbTransHash> queryTransList(Client client, String blockHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        ListPage<TbTransHash> tbTransHashListPage = new ListPage<>();
        List<TbTransHash> tbTransHashes = new ArrayList<>();

        try {
            if (blockHash != null) {
                // get TbTransHash list by blockHash
                BcosBlock bcosBlock = client.getBlockByHash(blockHash, true);

                generateTbTransHashListPage(pageIndex, pageSize, tbTransHashListPage, tbTransHashes, bcosBlock);
            } else {
                // get TbTransHash list by blockNumber
                BigInteger blockNum = blockNumber;
                if (blockNumber == null) {
                    blockNum = client.getBlockNumber().getBlockNumber();
                }
                BcosBlock bcosBlock = client.getBlockByNumber(blockNum, true);

                generateTbTransHashListPage(pageIndex, pageSize, tbTransHashListPage, tbTransHashes, bcosBlock);
            }
            tbTransHashListPage.setPageIndex(pageIndex);
            tbTransHashListPage.setPageSize(pageSize);
            return tbTransHashListPage;
        } catch (NullPointerException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
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

        List<JsonTransactionResponse> transactionHashList = block.getTransactions().stream()
                .map(transactionResult -> (JsonTransactionResponse) transactionResult.get()).collect(Collectors.toList()).subList(transIndexStart, transSize + transIndexStart);
        transactionHashList.forEach(tx -> {
            TbTransHash tbTransHash = new TbTransHash(tx.getHash(), tx.getFrom(), tx.getTo(),
                    tx.getBlockNumber(), DataTypeUtils.getTimestamp(Numeric.decodeQuantity(bcosBlock.getBlock().getTimestamp()).longValue()));
            tbTransHashes.add(tbTransHash);
        });
        tbTransHashListPage.setPageSize(transSize);
        tbTransHashListPage.setTotal(transCount);
        tbTransHashListPage.setPageData(tbTransHashes);
    }

    //Traverse block
    public static ListPage<TbBlock> queryBlockList(Client client, String blockHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        ListPage<TbBlock> tbBlockListPage = new ListPage<>();
        List<TbBlock> tbBlocks = new CopyOnWriteArrayList<>();
        Integer blockCount;
        BcosBlock.Block block;
        try {
            if (blockHash != null) {
                BcosBlock bcosBlock = client.getBlockByHash(blockHash, true);
                block = bcosBlock.getBlock();
                blockCount = 1;
                getTbBlockList(tbBlocks, block);
            } else if (blockNumber != null) {
                BcosBlock bcosBlock = client.getBlockByNumber(blockNumber, true);
                block = bcosBlock.getBlock();
                blockCount = 1;
                getTbBlockList(tbBlocks, block);
            } else {
                int blockNum = client.getBlockNumber().getBlockNumber().intValue();
                if (pageIndex < 1 || (pageIndex - 1) * pageSize > blockNum) {
                    log.error("pageIndex error.");
                    throw new BrokerException("pageIndex error.");
                }
                int blockSize = (blockNum <= pageIndex * pageSize) ? (blockNum - ((pageIndex - 1) * pageSize)) : pageSize;
                long blockNumberIndex = (long) pageSize * (pageIndex - 1) + 1;

                List<Long> blockNums = new ArrayList<>();
                for (int i = 0; i < blockSize; i++) {
                    blockNums.add(blockNumberIndex);
                    blockNumberIndex++;
                }
                blockCount = blockNum;
                tbBlocks = getTbBlock(client, blockNums);

                tbBlocks.sort((arg0, arg1) -> arg1.getBlockNumber().compareTo(arg0.getBlockNumber()));
            }

            tbBlockListPage.setPageIndex(pageIndex);
            tbBlockListPage.setPageSize(pageSize);
            tbBlockListPage.setTotal(blockCount);
            tbBlockListPage.setPageData(tbBlocks);
            return tbBlockListPage;
        } catch (ExecutionException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("query transaction failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    private static void getTbBlockList(List<TbBlock> tbBlocks, BcosBlock.Block block) throws BrokerException {
        if (block == null) {
            log.error("query block failed, block is null.");
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }

        String blockTimestamp = DataTypeUtils.getTimestamp(Numeric.decodeQuantity(block.getTimestamp()).longValue());
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

    public static synchronized ListPage<TbNode> queryNodeList(Client client) throws BrokerException {
        ListPage<TbNode> tbNodeListPage = new ListPage<>();
        //1、Current node, pbftview, and blockNumber
        List<TbNode> tbNodes = new ArrayList<>();
        try {

            List<String> observerList = client.getObserverList().getObserverList();
            List<String> sealerList = client.getSealerList().getSealerList();

            if (CollectionUtils.isEmpty(sealerList)) {
                log.error("nodeList query from web3j is empty.");
                throw new BrokerException("nodeList query from web3j is empty");
            }

            List<String> nodeIds = client.getNodeIDList().getNodeIDList();

            // get PbftView from each nodes
            Map<String, Map<String, String>> nodeViews = getNodeViews(client);
            // get blockNum from each nodes
            Map<String, Map<String, String>> nodeBlockNums = getBlockNums(client);

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
        } catch (Exception e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("query node failed due to web3sdk rpc error", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    private static Map<String, Map<String, String>> getNodeViews(Client client) {
        ConsensusStatus.ConsensusInfo result = client.getConsensusStatus().getResult();
        Map<String, Map<String, String>> nodeViews = new HashMap<>();

        result.getViewInfos().forEach(viewInfo -> {
            Map<String, String> map = new HashMap<>();
            map.put(NODE_ID, viewInfo.getNodeId());
            map.put(VIEW, viewInfo.getView());
            nodeViews.put(viewInfo.getNodeId(), map);
        });
        return nodeViews;
    }

    private static Map<String, Map<String, String>> getBlockNums(Client client) {
        Map<String, Map<String, String>> nodeBlockNums = new HashMap<>();
        SyncStatus.SyncStatusInfo statusInfo = client.getSyncStatus().getResult();

        Map<String, String> currentPeer = new HashMap<>();
        currentPeer.put(NODE_ID, statusInfo.getNodeId());
        currentPeer.put(BLOCK_NUMBER, Numeric.decodeQuantity(statusInfo.getBlockNumber()).toString());
        nodeBlockNums.put(statusInfo.getNodeId(), currentPeer);

        statusInfo.getPeers().forEach(peersInfo -> {
            Map<String, String> connectedPeer = new HashMap<>();
            connectedPeer.put(NODE_ID, peersInfo.getNodeId());
            connectedPeer.put(BLOCK_NUMBER, Numeric.decodeQuantity(peersInfo.getBlockNumber()).toString());
            nodeBlockNums.put(peersInfo.getNodeId(), connectedPeer);
        });
        return nodeBlockNums;
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
        return nodeIds.contains(nodeId) ? 1 : 0;
    }

    private static List<TbBlock> getTbBlock(Client client, List<Long> blockNums) throws ExecutionException, InterruptedException {

        List<CompletableFuture<TbBlock>> futureList = new ArrayList<>();
        for (Long blockNumber : blockNums) {
            CompletableFuture<TbBlock> future = CompletableFuture.supplyAsync(() -> {
                BcosBlock bcosBlock = client.getBlockByNumber(BigInteger.valueOf(blockNumber), true);
                BcosBlock.Block block = bcosBlock.getBlock();
                if (block == null) {
                    return null;
                }

                String blockTimestamp = DataTypeUtils.getTimestamp(Numeric.decodeQuantity(block.getTimestamp()).longValue());
                int transactions = 0;
                if (!block.getTransactions().isEmpty()) {
                    transactions = block.getTransactions().size();
                }
                int sealerIndex = Integer.parseInt(block.getSealer().substring(2), 16);
                TbBlock tbBlock = new TbBlock(block.getHash(), block.getNumber(), blockTimestamp, transactions, sealerIndex);
                tbBlock.setSealer(block.getSealer());
                return tbBlock;
            });

            futureList.add(future);
        }

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futureList.stream().map(CompletableFuture::join).collect(Collectors.toList())).get();
    }
}

