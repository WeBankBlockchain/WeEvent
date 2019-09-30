package com.webank.weevent.broker.fabric.sdk;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.dto.TransactionInfo;
import com.webank.weevent.broker.fabric.util.ChaincodeExecuter;
import com.webank.weevent.broker.util.DataTypeUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public TopicInfo getTopicInfo(String topicName) throws BrokerException {
        TopicInfo topicInfo;
        try {
            ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(), fabricConfig.getTopicControllerVersion());
            String response = ChaincodeExecuter.executeTransaction(hfClient, channel, chaincodeID, false, "getTopicInfo", topicName);
            topicInfo = JSONObject.parseObject(response, TopicInfo.class);
            System.out.println("##################");
            System.out.println("response:" + response);
            System.out.println("topicInfo:" + topicInfo.toString());
//            sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
//            sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, transactionInfo.getBlockNumber().intValue(), Integer.parseInt(transactionInfo.getPayLoad())));
//            sendResult.setTopic(topicName);


        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException | UnsupportedEncodingException exception) {
            log.error("publish event failed due to transaction execution error.", exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("publish event failed due to transaction execution timeout.", timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
        return topicInfo;
    }

    public void init(String channelName) {
        try {
            this.hfClient = FabricSDKWrapper.initializeClient(this.fabricConfig);
            this.channel = FabricSDKWrapper.initializeChannel(hfClient, channelName, this.fabricConfig);
        } catch (Exception e) {
            log.error("init fabric failed", e);
            BrokerApplication.exit();
        }
    }

    public boolean createTopic(String topicName) throws BrokerException {
        try {
            ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(),
                    fabricConfig.getTopicControllerVersion());
            String payload = ChaincodeExecuter.executeTransaction(hfClient, channel, chaincodeID, true,
                    "addTopicInfo", topicName, getTimestamp(System.currentTimeMillis()),fabricConfig.getTopicVerison());
            System.out.println("##########################");
            System.out.println("payload:"+payload);
            return StringUtils.isNotBlank(payload);
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException
                | UnsupportedEncodingException exception) {
            log.error("create topic :{} failed due to transaction execution error.{}", topicName, exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("create topic :{} failed due to transaction execution timeout. {}", topicName, timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public boolean isTopicExist(String topicName) throws BrokerException {
        try {
            ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(),
                    fabricConfig.getTopicControllerVersion());
            String payload = ChaincodeExecuter.executeTransaction(hfClient, channel, chaincodeID, false,
                    "isTopicExist", topicName);
            System.out.println("##########################");
            System.out.println("payload:"+payload);
            return StringUtils.isNotBlank(payload);
        } catch (InterruptedException | ProposalException | ExecutionException | InvalidArgumentException
                | UnsupportedEncodingException exception) {
            log.error("create topic :{} failed due to transaction execution error.{}", topicName, exception);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException timeout) {
            log.error("create topic :{} failed due to transaction execution timeout. {}", topicName, timeout);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public void listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {

    }

    public void getEvent(String eventId) throws BrokerException {

    }

    public SendResult publishEvent(String topicName, String eventContent, String extensions) throws BrokerException {
        SendResult sendResult = new SendResult();
        TransactionInfo transactionInfo = new TransactionInfo();
        try {
            ChaincodeID chaincodeID = getChaincodeID(fabricConfig);
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

    private static ChaincodeID getChaincodeID(FabricConfig fabricConfig) throws InvalidArgumentException, ProposalException, InterruptedException, ExecutionException, TimeoutException {
        ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(), fabricConfig.getTopicControllerVersion());
        String topicContractName = FabricSDKWrapper.executeTransaction(hfClient, channel, chaincodeID, false, "getTopicContractName", fabricConfig.getTransactionTimeout()).getPayLoad();
        String topicContractVersion = FabricSDKWrapper.executeTransaction(hfClient, channel, chaincodeID, false, "getTopicContractVersion", fabricConfig.getTransactionTimeout()).getPayLoad();

        return FabricSDKWrapper.getChainCodeID(topicContractName, topicContractVersion);
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

    /**
     * Gets the ISO 8601 timestamp.
     *
     * @param date the date
     * @return the ISO 8601 timestamp
     */
    private static String getTimestamp(long date) {
        // TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // df.setTimeZone(tz);
        return df.format(date);
    }

}
