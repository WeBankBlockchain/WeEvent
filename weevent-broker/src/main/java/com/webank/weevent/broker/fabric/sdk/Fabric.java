package com.webank.weevent.broker.fabric.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.dto.TransactionInfo;
import com.webank.weevent.broker.util.DataTypeUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
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
    private static HFClient hfClient = null;
    private static Channel channel = null;

    public Fabric(FabricConfig fabricConfig) {
        this.fabricConfig = fabricConfig;
    }

    public void getTopic(String topicName) throws BrokerException {

    }

    public void init(String channelName) {
        try {
            hfClient = FabricSDKWrapper.initializeClient(this.fabricConfig);
            channel = FabricSDKWrapper.initializeChannel(hfClient, channelName, fabricConfig);
        } catch (Exception e) {
            log.error("init fabric failed", e);
            BrokerApplication.exit();
        }
    }

    public boolean createTopic(String topicName) throws BrokerException {
        return false;
    }

    public boolean isTopicExist(String topicName) throws BrokerException {
        return false;
    }

    public void listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {

    }

    public void getTopicInfo(String topicName) throws BrokerException {

    }

    public List<String> listChannelName() throws BrokerException {
        List listChannel = new ArrayList();
        listChannel.add(fabricConfig.getChannelName());
        return listChannel;
    }

    public void getEvent(String eventId) throws BrokerException {

    }

    public SendResult publishEvent(String topicName, String eventContent, String extensions) throws BrokerException {
        SendResult sendResult = new SendResult();
        TransactionInfo transactionInfo = new TransactionInfo();
        try {
            ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(), fabricConfig.getTopicControllerVersion());
            String topicContractName = FabricSDKWrapper.executeTransaction(hfClient, channel, chaincodeID, false, "getTopicContractName", fabricConfig.getTransactionTimeout()).getPayLoad();
            String topicContractVersion = FabricSDKWrapper.executeTransaction(hfClient, channel, chaincodeID, false, "getTopicContractVersion", fabricConfig.getTransactionTimeout()).getPayLoad();

            chaincodeID = FabricSDKWrapper.getChainCodeID(topicContractName, topicContractVersion);
            transactionInfo = FabricSDKWrapper.executeTransaction(hfClient, channel, chaincodeID, true, "publish", fabricConfig.getTransactionTimeout(), topicName, eventContent, extensions);
            sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
            sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, transactionInfo.getBlockNumber().intValue(), Integer.parseInt(transactionInfo.getPayLoad())));
            sendResult.setTopic(topicName);
            return sendResult;
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException exception) {
            log.error("publish event failed due to transaction execution error.", exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("publish event failed due to transaction execution timeout.", timeout);
            sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
            return sendResult;
        }
    }

    public Long getBlockHeight() throws BrokerException {
        try {
            return channel.queryBlockchainInfo().getHeight();
        } catch (Exception e) {
            log.error("get block height error:{}", e);
            throw new BrokerException(ErrorCode.GET_BLOCK_HEIGHT_ERROR);
        }
    }

    /**
     * Fetch all event in target block.
     *
     * @param blockNum the blockNum
     * @return java.lang.Integer null if net error
     */
    public List<WeEvent> loop(Long blockNum) throws BrokerException {
        List<WeEvent> weEventList = new ArrayList<>();
        try {
            weEventList = FabricSDKWrapper.getBlockChainInfo(channel, blockNum);
        } catch (Exception e) {
            log.error("getEvent error:{}", e);
        }
        return weEventList;
    }
}
