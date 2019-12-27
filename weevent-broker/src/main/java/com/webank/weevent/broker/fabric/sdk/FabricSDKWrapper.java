package com.webank.weevent.broker.fabric.sdk;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.dto.TransactionInfo;
import com.webank.weevent.broker.fabric.util.FabricUser;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import com.google.protobuf.InvalidProtocolBufferException;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/9
 */
@Slf4j
public class FabricSDKWrapper {
    // Create HFClient
    public static HFClient initializeClient(FabricConfig fabricConfig) throws InvalidArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        hfClient.setUserContext(new FabricUser(fabricConfig));
        return hfClient;
    }

    // Create Channel
    public static Channel initializeChannel(HFClient client, String channelName, FabricConfig fabricConfig) throws InvalidArgumentException, TransactionException {
        Orderer orderer1 = getOrderer(client, fabricConfig);

        Peer peer0 = getPeer(client, fabricConfig);

        Channel channel = client.newChannel(channelName);
        channel.addOrderer(orderer1);
        channel.addPeer(peer0);
        channel.initialize();
        return channel;
    }

    private static Orderer getOrderer(HFClient client, FabricConfig fabricConfig) throws InvalidArgumentException {
        Properties orderer1Prop = new Properties();
        orderer1Prop.setProperty("pemFile", fabricConfig.getOrdererTlsCaFile());
        orderer1Prop.setProperty("sslProvider", "openSSL");
        orderer1Prop.setProperty("negotiationType", "TLS");
        orderer1Prop.setProperty("ordererWaitTimeMilliSecs", "300000");
        orderer1Prop.setProperty("hostnameOverride", "orderer");
        orderer1Prop.setProperty("trustServerCertificate", "true");
        orderer1Prop.setProperty("allowAllHostNames", "true");
        return client.newOrderer("orderer", fabricConfig.getOrdererAddress(), orderer1Prop);
    }

    public static Peer getPeer(HFClient client, FabricConfig fabricConfig) throws InvalidArgumentException {
        Properties peer0Prop = new Properties();
        peer0Prop.setProperty("pemFile", fabricConfig.getPeerTlsCaFile());
        peer0Prop.setProperty("sslProvider", "openSSL");
        peer0Prop.setProperty("negotiationType", "TLS");
        peer0Prop.setProperty("hostnameOverride", "peer0");
        peer0Prop.setProperty("trustServerCertificate", "true");
        peer0Prop.setProperty("allowAllHostNames", "true");
        return client.newPeer("peer0", fabricConfig.getPeerAddress(), peer0Prop);
    }

    public static ChaincodeID getChainCodeID(String chaincodeName, String chaincodeVersion) {
        return ChaincodeID.newBuilder().setName(chaincodeName).setVersion(chaincodeVersion).build();
    }

    public static Collection<ProposalResponse> installProposal(HFClient client, Channel channel, ChaincodeID chaincodeID,
                                                               TransactionRequest.Type chaincodeLang,         // Type.GO_LANG
                                                               String chaincodeVer,        // "v1"
                                                               String chaincodeSourceLoc,  // "/opt/gopath"
                                                               String chaincodePath        // "github.com/hyperledger/fabric/peer/chaincode/go/chaincode_example02
    ) throws InvalidArgumentException, ProposalException {
        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeVersion(chaincodeVer);
        installProposalRequest.setChaincodeLanguage(chaincodeLang);
        installProposalRequest.setChaincodeSourceLocation(new File(chaincodeSourceLoc));
        installProposalRequest.setChaincodePath(chaincodePath);
        return client.sendInstallProposal(installProposalRequest, channel.getPeers());
    }

    public static Collection<ProposalResponse> instantiateProposal(HFClient client, Channel channel, ChaincodeID chaincodeID,
                                                                   TransactionRequest.Type chaincodeLang, Long proposalTimeout) throws InvalidArgumentException, ProposalException {
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(proposalTimeout);//time in milliseconds
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setChaincodeLanguage(chaincodeLang);
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setArgs(new String[]{});

        // I do not know the purpose of transient map works for.
        Map<String, byte[]> transientMap = new HashMap<>();
        transientMap.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        transientMap.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(transientMap);
        return channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
    }

    public static BlockEvent.TransactionEvent sendTransaction(Channel channel, Collection<ProposalResponse> propResp, Long transactionTimeout) throws InvalidArgumentException, InterruptedException, ExecutionException, TimeoutException {
        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();
        for (ProposalResponse response : propResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                String payload = new String(response.getChaincodeActionResponsePayload());
                log.debug("[√] Got success response from peer:{}, payload:{}", response.getPeer().getName(), payload);
                successful.add(response);
            } else {
                String status = response.getStatus().toString();
                String msg = response.getMessage();
                log.error("[×] Got failed response from peer:{}, status:{}, error message:{} ", response.getPeer().getName(), status, msg);
                failed.add(response);
            }
        }

        CompletableFuture<BlockEvent.TransactionEvent> carfuture = channel.sendTransaction(successful);
        return carfuture.get(transactionTimeout, TimeUnit.MILLISECONDS);
    }

    public static TransactionInfo executeTransaction(HFClient client, Channel channel, ChaincodeID chaincodeID, boolean invoke, String func,
                                                     Long transactionTimeout, String... args) throws InvalidArgumentException, ProposalException, InterruptedException, ExecutionException, TimeoutException {
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);

        transactionProposalRequest.setFcn(func);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setProposalWaitTime(120000);

        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();
        // there is no need to retry. If not, you should re-send the transaction proposal.
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest);
        TransactionInfo transactionInfo = new TransactionInfo();
        boolean result = true;
        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                transactionInfo.setCode(ErrorCode.SUCCESS.getCode());
                transactionInfo.setPayLoad(new String(response.getChaincodeActionResponsePayload()));
                log.info("[√] Got success response from peer:{} , payload:{}", response.getPeer().getName(), transactionInfo.getPayLoad());
                successful.add(response);
            } else {
                result = false;
                transactionInfo.setCode(ErrorCode.FABRICSDK_CHAINCODE_INVOKE_FAILED.getCode());
                transactionInfo.setMessage(response.getMessage());
                String status = response.getStatus().toString();
                log.error("[×] Got failed response from peer:{}, status:{}, error message:{}", response.getPeer().getName(), status, transactionInfo.getMessage());
                failed.add(response);
            }
        }

        if (invoke && result) {
            log.info("Sending transaction to orderers...");
            CompletableFuture<BlockEvent.TransactionEvent> carfuture = channel.sendTransaction(successful);
            BlockEvent.TransactionEvent transactionEvent = carfuture.get(transactionTimeout, TimeUnit.MILLISECONDS);
            transactionInfo.setBlockNumber(transactionEvent.getBlockEvent().getBlockNumber());
            log.info("Wait event return: " + transactionEvent.getChannelId() + " " + transactionEvent.getTransactionID() + " " + transactionEvent.getType() + " " + transactionEvent.getValidationCode());
        }
        return transactionInfo;
    }

    public static CompletableFuture<TransactionInfo> executeTransactionAsync(HFClient client, Channel channel, ChaincodeID chaincodeID, boolean invoke, String func,
                                                                             String... args) throws InvalidArgumentException, ProposalException {
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);

        transactionProposalRequest.setFcn(func);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setProposalWaitTime(120000);

        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();
        // there is no need to retry. If not, you should re-send the transaction proposal.
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest);
        TransactionInfo transactionInfo = new TransactionInfo();
        boolean result = true;
        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                transactionInfo.setCode(ErrorCode.SUCCESS.getCode());
                transactionInfo.setPayLoad(new String(response.getChaincodeActionResponsePayload()));
                log.info("[√] Got success response from peer:{} , payload:{}", response.getPeer().getName(), transactionInfo.getPayLoad());
                successful.add(response);
            } else {
                result = false;
                transactionInfo.setCode(ErrorCode.FABRICSDK_CHAINCODE_INVOKE_FAILED.getCode());
                transactionInfo.setMessage(response.getMessage());
                String status = response.getStatus().toString();
                log.error("[×] Got failed response from peer:{}, status:{}, error message:{}", response.getPeer().getName(), status, transactionInfo.getMessage());
                failed.add(response);
            }
        }

        if (invoke && result) {
            log.info("Sending transaction to orders...");
            return channel.sendTransaction(successful).thenApply(
                    (transactionEvent) -> {
                        log.info("Wait event return: {} {} {} {}",
                                transactionEvent.getChannelId(),
                                transactionEvent.getTransactionID(),
                                transactionEvent.getType(),
                                transactionEvent.getValidationCode());
                        transactionInfo.setBlockNumber(transactionEvent.getBlockEvent().getBlockNumber());
                        return transactionInfo;
                    });
        }

        CompletableFuture<TransactionInfo> completableFuture = new CompletableFuture<>();
        completableFuture.complete(transactionInfo);
        return completableFuture;
    }

    public static List<WeEvent> getBlockChainInfo(Channel channel, Long blockNumber) throws ProposalException, InvalidArgumentException {
        List<WeEvent> weEventList = new ArrayList<>();
        BlockInfo returnedBlock = channel.queryBlockByNumber(blockNumber);
        for (BlockInfo.EnvelopeInfo envelopeInfo : returnedBlock.getEnvelopeInfos()) {
            if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;
                for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo.getTransactionActionInfos()) {
                    log.debug("chaincode input arguments count:{}", transactionActionInfo.getChaincodeInputArgsCount());
                    if (transactionActionInfo.getChaincodeInputArgsCount() == WeEventConstants.DEFAULT_CHAINCODE_PARAM_COUNT && "publish".equals(new String(transactionActionInfo.getChaincodeInputArgs(0)))) {
                        WeEvent weEvent = new WeEvent();
                        weEvent.setTopic(new String(transactionActionInfo.getChaincodeInputArgs(1), UTF_8));
                        weEvent.setContent(transactionActionInfo.getChaincodeInputArgs(2));
                        weEvent.setExtensions(DataTypeUtils.json2Map(new String(transactionActionInfo.getChaincodeInputArgs(3))));
                        weEvent.setEventId(DataTypeUtils.encodeEventId(weEvent.getTopic(),
                                blockNumber.intValue(),
                                Integer.parseInt(new String(transactionActionInfo.getProposalResponsePayload()))));
                        weEventList.add(weEvent);
                        log.debug("weevent:{}", weEvent);
                    }
                }
            }
        }
        return weEventList;
    }

    public static GroupGeneral getGroupGeneral(Channel channel) throws InvalidArgumentException, ProposalException {
        GroupGeneral groupGeneral = new GroupGeneral();
        long currentBlockNum = channel.queryBlockchainInfo().getHeight() - 1;
        groupGeneral.setLatestBlock(BigInteger.valueOf(currentBlockNum));
        groupGeneral.setNodeCount(channel.getPeers().size());

        return groupGeneral;
    }

    public static ListPage<TbTransHash> queryTransList(FabricConfig fabricConfig,
                                                       Channel channel,
                                                       BigInteger blockNumber,
                                                       String blockHash, Integer pageIndex,
                                                       Integer pageSize) throws ProposalException, InvalidArgumentException, BrokerException, DecoderException {
        ListPage<TbTransHash> tbTransHashListPage = new ListPage<>();
        List<TbTransHash> tbTransHashes = new ArrayList<>();

        BlockInfo blockInfo;
        if (!StringUtils.isBlank(blockHash)) {
            blockInfo = channel.queryBlockByHash(Hex.decodeHex(blockHash));
        } else {
            blockInfo = getBlockInfo(fabricConfig, channel, blockNumber);
        }

        if (blockInfo == null) {
            log.error("query block by blockHash failed, block is empty.");
            throw new BrokerException("query block by blockHash failed, block is empty.");
        }
        generateTbTransHashListPage(pageIndex, pageSize, tbTransHashListPage, tbTransHashes, blockInfo);

        tbTransHashListPage.setPageData(tbTransHashes);
        return tbTransHashListPage;
    }

    private static void generateTbTransHashListPage(Integer pageIndex,
                                                    Integer pageSize,
                                                    ListPage<TbTransHash> tbTransHashListPage,
                                                    List<TbTransHash> tbTransHashes,
                                                    BlockInfo blockInfo) throws BrokerException {
        Integer transCount = blockInfo.getTransactionCount();

        if (pageIndex < 1 || (pageIndex - 1) * pageSize > transCount) {
            log.error("pageIndex error.");
            throw new BrokerException("pageIndex error.");
        }
        Integer transSize = (transCount <= pageIndex * pageSize) ? (transCount - ((pageIndex - 1) * pageSize)) : pageSize;
        Integer transIndexStart = (pageIndex - 1) * pageSize;


        Iterable<BlockInfo.EnvelopeInfo> envelopeInfos = blockInfo.getEnvelopeInfos();
        envelopeInfos.forEach(envelopeInfo -> {
            TbTransHash tbTransHash = new TbTransHash();
            tbTransHash.setCreateTime(DataTypeUtils.getTimestamp(envelopeInfo.getTimestamp()));
            tbTransHash.setBlockTimestamp(DataTypeUtils.getTimestamp(envelopeInfo.getTimestamp()));
            tbTransHash.setTransHash(envelopeInfo.getTransactionID());
            tbTransHash.setBlockNumber(BigInteger.valueOf(blockInfo.getBlockNumber()));
            tbTransHashes.add(tbTransHash);
        });

        if (tbTransHashes != null && !tbTransHashes.isEmpty()) {
            tbTransHashes.subList(transIndexStart, transSize + transIndexStart);
        }

        tbTransHashListPage.setPageSize(transSize);
        tbTransHashListPage.setTotal(transCount);
        tbTransHashListPage.setPageData(tbTransHashes);
    }

    public static ListPage<TbBlock> queryBlockList(FabricConfig fabricConfig,
                                                   Channel channel,
                                                   BigInteger blockNumber,
                                                   String blockHash,
                                                   Integer pageIndex,
                                                   Integer pageSize) throws ProposalException, InvalidArgumentException, ExecutionException, InterruptedException, DecoderException, InvalidProtocolBufferException {
        ListPage<TbBlock> tbBlockListPage = new ListPage<>();
        List<TbBlock> tbBlocks = new CopyOnWriteArrayList<>();
        int blockTotalCount;

        BlockInfo lastestblockInfo = getBlockInfo(fabricConfig, channel, null);
        BlockInfo blockInfo;

        TbBlock tbBlock = new TbBlock();
        if (!StringUtils.isBlank(blockHash)) {
            blockInfo = channel.queryBlockByHash(Hex.decodeHex(blockHash));
            generateTbBlock(channel, BigInteger.valueOf(blockInfo.getBlockNumber()), lastestblockInfo, blockInfo, tbBlock);
            tbBlocks.add(tbBlock);
            blockTotalCount = 1;
        } else if (blockNumber != null) {
            blockInfo = getBlockInfo(fabricConfig, channel, blockNumber);
            generateTbBlock(channel, blockNumber, lastestblockInfo, blockInfo, tbBlock);
            tbBlocks.add(tbBlock);
            blockTotalCount = 1;
        } else {
            BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
            blockInfo = getBlockInfo(fabricConfig, channel, null);
            long lastestblcokNum = blockInfo.getBlockNumber();

            int blockSize = ((int) lastestblcokNum <= pageIndex * pageSize) ? ((int) lastestblcokNum - ((pageIndex - 1) * pageSize)) : pageSize;
            long blockNumberIndex = (pageIndex - 1) * pageSize + 1;

            List<Long> blockNums = new ArrayList<>();
            for (int i = 0; i < blockSize; i++) {
                blockNums.add(blockNumberIndex);
                blockNumberIndex++;
            }

            tbBlocks = getTbBlocKList(channel, blockNums, blockchainInfo);
            blockTotalCount = Integer.parseInt(String.valueOf(lastestblockInfo.getBlockNumber()));
            tbBlocks.sort((arg0, arg1) -> arg1.getBlockNumber().compareTo(arg0.getBlockNumber()));
        }

        tbBlockListPage.setPageSize(pageSize);
        tbBlockListPage.setPageIndex(pageIndex);
        tbBlockListPage.setTotal(blockTotalCount);
        tbBlockListPage.setPageData(tbBlocks);
        return tbBlockListPage;
    }

    private static void generateTbBlock(Channel channel, BigInteger blockNumber, BlockInfo lastestblockInfo, BlockInfo blockInfo, TbBlock tbBlock) throws InvalidArgumentException, ProposalException, InvalidProtocolBufferException {
        if (blockNumber.longValue() != lastestblockInfo.getBlockNumber()) {
            BlockInfo nextBlockInfo = channel.queryBlockByNumber(blockNumber.longValue() + 1);
            tbBlock.setPkHash(Hex.encodeHexString(nextBlockInfo.getPreviousHash()));
        } else {
            BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
            tbBlock.setPkHash(Hex.encodeHexString(blockchainInfo.getCurrentBlockHash()));
        }
        if (blockInfo.getEnvelopeCount() > 0) {
            tbBlock.setBlockTimestamp(DataTypeUtils.getTimestamp(blockInfo.getEnvelopeInfo(0).getTimestamp()));
        }
        tbBlock.setTransCount(blockInfo.getEnvelopeCount());
        tbBlock.setBlockNumber(blockNumber);
    }

    private static List<TbBlock> getTbBlocKList(Channel channel, List<Long> blockNums, BlockchainInfo blockchainInfo) throws ExecutionException, InterruptedException {
        List<CompletableFuture<TbBlock>> futureList = new ArrayList<>();
        blockNums.forEach(blockNumber -> {
            CompletableFuture<TbBlock> future = CompletableFuture.supplyAsync(() -> {
                TbBlock tbBlock = new TbBlock();
                try {
                    BlockInfo blockInfo = channel.queryBlockByNumber(blockNumber);
                    if (blockNumber != (blockchainInfo.getHeight() - 1)) {
                        BlockInfo nextBlockInfo = channel.queryBlockByNumber(blockNumber + 1);
                        tbBlock.setPkHash(Hex.encodeHexString(nextBlockInfo.getPreviousHash()));
                    } else {
                        tbBlock.setPkHash(Hex.encodeHexString(blockchainInfo.getCurrentBlockHash()));
                    }
                    if (blockInfo.getEnvelopeCount() > 0) {
                        tbBlock.setBlockTimestamp(DataTypeUtils.getTimestamp(blockInfo.getEnvelopeInfo(0).getTimestamp()));
                    }
                    tbBlock.setBlockNumber(new BigInteger(String.valueOf(blockNumber)));
                    tbBlock.setTransCount(blockInfo.getTransactionCount());
                } catch (InvalidArgumentException | ProposalException | InvalidProtocolBufferException e) {
                    log.error("query block by blockNumber failed, e:", e);
                    return null;
                }

                return tbBlock;
            });

            futureList.add(future);
        });

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futureList.stream().map(CompletableFuture::join).collect(Collectors.toList())).get();
    }

    public static ListPage<TbNode> queryNodeList(FabricConfig fabricConfig,
                                                 Channel channel,
                                                 Integer pageIndex,
                                                 Integer pageSize) throws ProposalException, InvalidArgumentException {
        ListPage<TbNode> tbNodeListPage = new ListPage<>();
        List<TbNode> tbNodes = new ArrayList<>();
        BlockInfo blockInfo = getBlockInfo(fabricConfig, channel, null);

        Collection<Peer> peers = channel.getPeers();
        peers.forEach(peer -> {
            TbNode tbNode = new TbNode();
            tbNode.setBlockNumber(BigInteger.valueOf(blockInfo.getBlockNumber()));
            tbNode.setNodeId(peer.getUrl());
            tbNode.setNodeActive(1);
            tbNode.setNodeType(WeEventConstants.NODE_TYPE_SEALER);
            tbNodes.add(tbNode);
        });

        tbNodeListPage.setPageIndex(pageIndex);
        tbNodeListPage.setPageSize(pageSize);
        tbNodeListPage.setTotal(peers.size());
        tbNodeListPage.setPageData(tbNodes);
        return tbNodeListPage;
    }

    private static BlockInfo getBlockInfo(FabricConfig fabricConfig, Channel channel, BigInteger blockNumber) throws ProposalException, InvalidArgumentException {
        BlockInfo blockInfo;
        if (blockNumber == null) {
            long currentBlockNum = channel.queryBlockchainInfo(new FabricUser(fabricConfig)).getHeight() - 1;
            blockInfo = channel.queryBlockByNumber(currentBlockNum);
        } else {
            blockInfo = channel.queryBlockByNumber(blockNumber.longValue());
        }
        return blockInfo;
    }

    public static List<Pair<String, String>> queryInstalledChaincodes(HFClient client, Peer peer) throws ProposalException, InvalidArgumentException {
        List<Query.ChaincodeInfo> listChainCodeInfo = client.queryInstalledChaincodes(peer);
        List<Pair<String, String>> chainCodeList = new ArrayList<>();
        listChainCodeInfo.forEach(chaincodeInfo ->
            chainCodeList.add(new Pair<>(chaincodeInfo.getName(), chaincodeInfo.getVersion()))
        );
        return chainCodeList;
    }

}
