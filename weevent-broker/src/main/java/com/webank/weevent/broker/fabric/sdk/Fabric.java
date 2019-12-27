package com.webank.weevent.broker.fabric.sdk;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.dto.TransactionInfo;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/20
 */
@Slf4j
public class Fabric {
    //config
    private FabricConfig fabricConfig;
    private static HFClient hfClient;
    private static Channel channel;
    // ChaincodeID
    private static ChaincodeID topicChaincodeID;
    private static ChaincodeID topicControllerChaincodeID;
    // topic info list in local memory
    private Map<String, TopicInfo> topicInfo = new ConcurrentHashMap<>();

    public Fabric(FabricConfig fabricConfig) {
        this.fabricConfig = fabricConfig;
    }

    public void init(String channelName) throws BrokerException {
        try {
            hfClient = FabricSDKWrapper.initializeClient(this.fabricConfig);
            channel = FabricSDKWrapper.initializeChannel(hfClient, channelName, this.fabricConfig);
            topicControllerChaincodeID = FabricSDKWrapper
                    .getChainCodeID(fabricConfig.getTopicControllerName(), fabricConfig.getTopicControllerVersion());
            topicChaincodeID = getTopicChaincodeID(fabricConfig, topicControllerChaincodeID);
        } catch (Exception e) {
            log.error("init fabric failed", e);
            throw new BrokerException("init fabric failed");
        }
    }

    public TopicInfo getTopicInfo(String topicName) throws BrokerException {
        if (this.topicInfo.containsKey(topicName)) {
            return this.topicInfo.get(topicName);
        }

        if (!isTopicExist(topicName)) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        try {
            TransactionInfo transactionInfo = FabricSDKWrapper.executeTransaction(hfClient, channel, topicControllerChaincodeID, false,
                    "getTopicInfo", fabricConfig.getTransactionTimeout(), topicName);
            if (ErrorCode.SUCCESS.getCode() != transactionInfo.getCode()) {
                throw new BrokerException(transactionInfo.getCode(), transactionInfo.getMessage());
            }
            TopicInfo topicInfo = DataTypeUtils.json2Object(transactionInfo.getPayLoad(), TopicInfo.class);

            this.topicInfo.put(topicName, topicInfo);
            return topicInfo;
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException exception) {
            log.error("publish event failed due to transaction execution error.", exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("publish event failed due to transaction execution timeout.", timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public boolean createTopic(String topicName) throws BrokerException {
        try {
            TransactionInfo transactionInfo = FabricSDKWrapper.executeTransaction(hfClient, channel, topicControllerChaincodeID, true, "addTopicInfo",
                    fabricConfig.getTransactionTimeout(), topicName, fabricConfig.getTopicVerison());
            if (ErrorCode.SUCCESS.getCode() != transactionInfo.getCode()) {
                if (WeEventConstants.TOPIC_ALREADY_EXIST.equals(transactionInfo.getMessage())) {
                    throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
                }
                throw new BrokerException(transactionInfo.getCode(), transactionInfo.getMessage());
            }
            return true;
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException exception) {
            log.error("create topic :{} failed due to transaction execution error.{}", topicName, exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("create topic :{} failed due to transaction execution timeout. {}", topicName, timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public boolean isTopicExist(String topicName) throws BrokerException {
        try {
            TransactionInfo transactionInfo = FabricSDKWrapper.executeTransaction(hfClient, channel, topicControllerChaincodeID, false,
                    "isTopicExist", fabricConfig.getTransactionTimeout(), topicName);

            return ErrorCode.SUCCESS.getCode() == transactionInfo.getCode();
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException exception) {
            log.error("create topic :{} failed due to transaction execution error.{}", topicName, exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("create topic :{} failed due to transaction execution timeout. {}", topicName, timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public TopicPage listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {
        TopicPage topicPage = new TopicPage();
        try {
            TransactionInfo transactionInfo = FabricSDKWrapper.executeTransaction(hfClient, channel, topicControllerChaincodeID, false,
                    "listTopicName", fabricConfig.getTransactionTimeout(), String.valueOf(pageIndex), String.valueOf(pageSize));

            if (ErrorCode.SUCCESS.getCode() != transactionInfo.getCode()) {
                throw new BrokerException(transactionInfo.getCode(), transactionInfo.getMessage());
            }

            ListPage<String> listPage = DataTypeUtils.json2ListPage(transactionInfo.getPayLoad(), String.class);
            topicPage.setPageIndex(pageIndex);
            topicPage.setPageSize(listPage.getPageSize());
            topicPage.setTotal(listPage.getTotal());
            for (String topic : listPage.getPageData()) {
                topicPage.getTopicInfoList().add(getTopicInfo(topic));
            }

            return topicPage;
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException exception) {
            log.error("list topicName failed due to transaction execution error", exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("list topicName failed due to transaction execution timeout", timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public WeEvent getEvent(String eventId) throws BrokerException {
        ParamCheckUtils.validateEventId("", eventId, getBlockHeight());

        Long blockNum = DataTypeUtils.decodeBlockNumber(eventId);
        List<WeEvent> events = this.loop(blockNum);
        for (WeEvent event : events) {
            if (eventId.equals(event.getEventId())) {
                log.info("event:{}", event);
                return event;
            }
        }

        throw new BrokerException(ErrorCode.EVENT_ID_NOT_EXIST);

    }

    public CompletableFuture<SendResult> publishEvent(String topicName, String eventContent, String extensions) throws BrokerException {
        if (!isTopicExist(topicName)) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        SendResult sendResult = new SendResult();
        sendResult.setTopic(topicName);
        try {
            return FabricSDKWrapper.executeTransactionAsync(hfClient,
                    channel,
                    topicChaincodeID,
                    true,
                    "publish",
                    topicName,
                    eventContent,
                    extensions).thenApply(transactionInfo -> {
                sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, transactionInfo.getBlockNumber().intValue(), Integer.parseInt(transactionInfo.getPayLoad())));
                sendResult.setTopic(topicName);
                return sendResult;
            });
        } catch (ProposalException | InvalidArgumentException exception) {
            log.error("publish event failed due to transaction execution error.", exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    private static ChaincodeID getTopicChaincodeID(FabricConfig fabricConfig, ChaincodeID topicControllerChaincodeID) throws InvalidArgumentException, ProposalException, InterruptedException, ExecutionException, TimeoutException {
        String topicContractName = FabricSDKWrapper.executeTransaction(hfClient, channel, topicControllerChaincodeID, false, "getTopicContractName", fabricConfig.getTransactionTimeout()).getPayLoad();
        String topicContractVersion = FabricSDKWrapper.executeTransaction(hfClient, channel, topicControllerChaincodeID, false, "getTopicContractVersion", fabricConfig.getTransactionTimeout()).getPayLoad();

        return FabricSDKWrapper.getChainCodeID(topicContractName, topicContractVersion);
    }

    public Long getBlockHeight() throws BrokerException {
        try {
            return channel.queryBlockchainInfo().getHeight() - 1;
        } catch (Exception e) {
            log.error("get block height error", e);
            throw new BrokerException(ErrorCode.GET_BLOCK_HEIGHT_ERROR);
        }
    }

    /**
     * Fetch all event in target block.
     *
     * @param blockNum the blockNum
     * @return java.lang.Integer null if net error
     */
    public List<WeEvent> loop(Long blockNum) {
        List<WeEvent> weEventList = new ArrayList<>();
        try {
            weEventList = FabricSDKWrapper.getBlockChainInfo(channel, blockNum);
        } catch (Exception e) {
            log.error("getEvent error", e);
        }
        return weEventList;
    }

    public GroupGeneral getGroupGeneral() throws BrokerException {

        try {
            return FabricSDKWrapper.getGroupGeneral(channel);
        } catch (ProposalException | InvalidArgumentException e) {
            log.error("get group general error", e);
            throw new BrokerException(ErrorCode.FABRICSDK_GETBLOCKINFO_ERROR);
        }
    }

    public ListPage<TbTransHash> queryTransList(BigInteger blockNumber, String blockHash, Integer pageIndex, Integer pageSize) throws BrokerException {

        try {
            return FabricSDKWrapper.queryTransList(fabricConfig, channel, blockNumber, blockHash, pageIndex, pageSize);
        } catch (InvalidArgumentException | ProposalException | DecoderException e) {
            log.error("query trans list by transHash and blockNum error", e);
            throw new BrokerException(ErrorCode.FABRICSDK_GETBLOCKINFO_ERROR);
        }
    }

    public ListPage<TbBlock> queryBlockList(BigInteger blockNumber, String blockHash, Integer pageIndex, Integer pageSize) throws BrokerException {

        try {
            return FabricSDKWrapper.queryBlockList(fabricConfig, channel, blockNumber, blockHash, pageIndex, pageSize);
        } catch (InvalidArgumentException | ProposalException | ExecutionException | InterruptedException | DecoderException | InvalidProtocolBufferException e) {
            log.error("query block list by transHash and blockNum error", e);
            throw new BrokerException(ErrorCode.FABRICSDK_GETBLOCKINFO_ERROR);
        }
    }

    public ListPage<TbNode> queryNodeList(Integer pageIndex, Integer pageSize) throws BrokerException {

        try {
            return FabricSDKWrapper.queryNodeList(fabricConfig, channel, pageIndex, pageSize);
        } catch (InvalidArgumentException | ProposalException e) {
            log.error("query node list by transHash and blockNum error", e);
            throw new BrokerException(ErrorCode.FABRICSDK_GETBLOCKINFO_ERROR);
        }
    }

    public List<String> listChannelName(FabricConfig fabricConfig) throws BrokerException {
        try {
            Peer peer = FabricSDKWrapper.getPeer(hfClient, fabricConfig);
            Set<String> channels = hfClient.queryChannels(peer);
            return new ArrayList<>(channels);
        } catch (Exception e) {
            log.error("get channel name list failed , e: ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }
}
