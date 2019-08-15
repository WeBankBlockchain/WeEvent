package com.webank.weevent.broker.fisco.web3sdk;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.contract.v2.Topic;
import com.webank.weevent.broker.fisco.contract.v2.TopicController;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tx.Contract;

/**
 * Access to FISCO-BCOS 2.x.
 *
 * @author matthewliu
 * @since 2019/04/28
 */
@Slf4j
public class FiscoBcos2 {
    // config
    private FiscoConfig fiscoConfig;

    // binding group id
    private Long groupId;

    // tx account
    private Credentials credentials;

    // real handler
    private Web3j web3j;

    // topic control
    private TopicController topicController;

    // topic list
    private Map<String, Topic> topicMap = new ConcurrentHashMap<>();

    public FiscoBcos2(FiscoConfig fiscoConfig) {
        this.fiscoConfig = fiscoConfig;
    }

    public void init(Long groupId, String address) throws BrokerException {
        if (this.topicController == null) {
            this.groupId = groupId;
            this.credentials = Web3SDK2Wrapper.getCredentials(this.fiscoConfig);
            this.web3j = Web3SDK2Wrapper.initWeb3j(groupId, this.fiscoConfig, FiscoBcosDelegate.threadPool);
            this.topicController = (TopicController) getContractService(address, TopicController.class);
        }
    }

    /**
     * Gets the contract service.
     *
     * @param contractAddress the contract address
     * @param cls the class
     * @return the contract service
     */
    protected Contract getContractService(String contractAddress, Class<?> cls) throws BrokerException {
        if (this.web3j == null || this.credentials == null) {
            log.error("init web3sdk failed");
            throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
        }

        Contract contract = Web3SDK2Wrapper.loadContract(contractAddress, this.web3j, this.credentials, cls);
        if (contract == null) {
            String msg = "load contract failed, " + cls.getSimpleName();
            log.error(msg);
            throw new BrokerException(ErrorCode.LOAD_CONTRACT_ERROR);
        }

        return contract;
    }

    /**
     * Topic Handler cache.
     *
     * @param topicName the topicName
     * @return null if not exist
     * @throws BrokerException BrokerException
     */
    private Topic getTopic(String topicName) throws BrokerException {
        if (this.topicMap.containsKey(topicName)) {
            return this.topicMap.get(topicName);
        }

        if (this.topicController == null) {
            log.error("topicController is null");
            throw new BrokerException(ErrorCode.TOPIC_CONTROLLER_IS_NULL);
        }

        try {
            String topicAddress = this.topicController.getTopicAddress(topicName).sendAsync().get();
            if (topicAddress == null) {
                log.error("topic contact address is null, check configuration `fisco.topic-controller.contract-address`");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            //topic is not exist if address is empty
            if (topicAddress.equals(WeEventConstants.ADDRESS_EMPTY)) {
                return null;
            }

            Topic topic = (Topic) getContractService(topicAddress, Topic.class);
            this.topicMap.put(topicName, topic);
            return topic;
        } catch (InterruptedException | ExecutionException e) {
            log.error("InterruptedException|ExecutionException raise", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    public boolean isTopicExist(String topicName) throws BrokerException {
        return getTopic(topicName) != null;
    }

    public boolean createTopic(String topicName) throws BrokerException {
        try {
            // check if topic contract exist
            if (isTopicExist(topicName)) {
                log.info("topic name already exist, {}", topicName);
                throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
            }

            // deploy topic contract
            Topic topic = Topic.deploy(this.web3j, this.credentials, Web3SDK2Wrapper.gasProvider).sendAsync().get();
            if (topic.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after Topic.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            TransactionReceipt transactionReceipt = topicController
                    .addTopicInfo(topicName, topic.getContractAddress())
                    .sendAsync().get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
            List<TopicController.LogAddTopicNameAddressEventResponse> event = topicController
                    .getLogAddTopicNameAddressEvents(transactionReceipt);

            if (CollectionUtils.isNotEmpty(event)) {
                if (event.get(0).retCode.intValue() == ErrorCode.TOPIC_ALREADY_EXIST.getCode()) {
                    log.info("topic name already exist, {}", topicName);
                    throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
                }
            }

            return true;
        } catch (InterruptedException | ExecutionException e) {
            log.error("create topic failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException e) {
            log.error("create topic failed due to transaction timeout. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public ListPage listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {
        try {
            ListPage<String> listPage = new ListPage<>();
            Tuple3<BigInteger, List<byte[]>, List<byte[]>> result = this.topicController.listTopicName(BigInteger.valueOf(pageIndex),
                    BigInteger.valueOf(pageSize)).sendAsync().get();
            if (result == null) {
                log.error("TopicController.listTopicName result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }
            listPage.setPageIndex(pageIndex);
            listPage.setPageSize(pageSize);
            listPage.setTotal(result.getValue1().intValue());
            List<byte[]> topicNames1List = result.getValue2();
            List<byte[]> topicNames2List = result.getValue3();
            for (int i = 0; i < topicNames1List.size(); i++) {
                String topicName = new String(topicNames1List.get(i), StandardCharsets.UTF_8) + new String(topicNames2List.get(i), StandardCharsets.UTF_8);
                if (topicName.isEmpty()) {
                    log.error("detect topic name is empty, {}", topicName);
                    continue;
                }
                listPage.getPageData().add(topicName.trim());
            }
            return listPage;
        } catch (InterruptedException | ExecutionException e) {
            log.error("list topic name failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    public TopicInfo getTopicInfo(String topicName) throws BrokerException {
        try {
            Tuple3<String, String, BigInteger> topic = this.topicController.getTopicInfo(topicName).sendAsync().get();
            if (topic == null) {
                log.error("TopicController.getTopicInfo result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            String topicAddress = topic.getValue1();
            if (WeEventConstants.ADDRESS_EMPTY.equals(topicAddress)) {
                log.error("TopicController.getTopicInfo address is empty");
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setTopicName(topicName);
            topicInfo.setTopicAddress(topicAddress);
            topicInfo.setCreatedTimestamp(topic.getValue3().longValue());
            topicInfo.setSenderAddress(topic.getValue2());

            //get topic.sol contract info
            Topic topicData = getTopic(topicName);
            if (topicData != null) {
                topicInfo.setBlockNumber(topicData.getBlockNumber().sendAsync().get().longValue());
                topicInfo.setSequenceNumber(topicData.getSequenceNumber().sendAsync().get().longValue());
            }
            return topicInfo;
        } catch (InterruptedException | ExecutionException e) {
            log.error("get topic info failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
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

    public SendResult publishEvent(String topicName, String eventContent, String extensions) throws BrokerException {
        Topic topic = getTopic(topicName);
        if (topic == null) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        SendResult sendResult = new SendResult();
        try {
            TransactionReceipt transactionReceipt = topic.publishWeEvent(topicName,
                    eventContent, extensions).sendAsync().get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
            List<Topic.LogWeEventEventResponse> event = Web3SDK2Wrapper.receipt2LogWeEventEventResponse(web3j, credentials, transactionReceipt);
            if (CollectionUtils.isNotEmpty(event)) {
                sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, event.get(0).eventBlockNumer.intValue(), event.get(0).eventSeq.intValue()));
                sendResult.setTopic(topicName);
                return sendResult;
            } else {
                sendResult.setStatus(SendResult.SendResultStatus.ERROR);
                return sendResult;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("publish event failed due to transaction execution error.", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException e) {
            log.error("publish event failed due to transaction execution timeout.", e);
            sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
            return sendResult;
        }
    }

    /**
     * getBlockHeight
     *
     * @return 0L if net error
     */
    public Long getBlockHeight() throws BrokerException {
        return Web3SDK2Wrapper.getBlockHeight(this.web3j);
    }

    /**
     * Fetch all event in target block.
     *
     * @param blockNum the blockNum
     * @return java.lang.Integer null if net error
     */
    public List<WeEvent> loop(Long blockNum) throws BrokerException {
        return Web3SDK2Wrapper.loop(this.web3j, this.credentials, blockNum);
    }
}
