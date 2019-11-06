package com.webank.weevent.broker.fisco.web3sdk;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.v1.Topic;
import com.webank.weevent.broker.fisco.web3sdk.v1.TopicController;
import com.webank.weevent.broker.fisco.web3sdk.v1.Web3SDKWrapper;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;

/**
 * Access to FISCO-BCOS 1.x.
 *
 * @author matthewliu
 * @since 2019/04/28
 */
@Slf4j
public class FiscoBcos {
    // config
    private FiscoConfig fiscoConfig;

    // tx account
    private Credentials credentials;

    // real handler
    private Web3j web3j;

    // topic control
    private TopicController topicController;

    // topic list, local cache will not be expired forever
    private Map<String, Topic> topicMap = new ConcurrentHashMap<>();

    public FiscoBcos(FiscoConfig fiscoConfig) {
        this.fiscoConfig = fiscoConfig;
    }

    public void init() throws BrokerException {
        if (this.topicController == null) {
            this.credentials = Web3SDKWrapper.getCredentials(this.fiscoConfig);
            this.web3j = Web3SDKWrapper.initWeb3j(this.fiscoConfig, FiscoBcosDelegate.threadPool);

            String address = Web3SDKWrapper.getAddress(this.web3j, this.credentials, this.fiscoConfig.getProxyAddress());
            if (StringUtils.isBlank(address)) {
                log.error("no topic control address in CNS, deploy it first");
                throw new BrokerException(ErrorCode.TOPIC_CONTROLLER_IS_NULL);
            }

            log.info("topic control address in CNS: {}", address);
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
    private Contract getContractService(String contractAddress, Class<?> cls) throws BrokerException {
        if (this.web3j == null || this.credentials == null) {
            log.error("init web3sdk failed");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }

        return Web3SDKWrapper.loadContract(contractAddress, this.web3j, this.credentials, cls);
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
            Address address = this.topicController.getTopicAddress(new Utf8String(topicName)).get();
            if (address == null) {
                log.error("topic contact address is null, check configuration `fisco.topic-controller.contract-address`");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            String topicAddress = address.toString();
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
            Topic topic = Topic.deploy(this.web3j, this.credentials, WeEventConstants.GAS_PRICE, WeEventConstants.GAS_LIMIT,
                    WeEventConstants.INITIAL_VALUE).get();
            if (topic.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after Topic.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            TransactionReceipt transactionReceipt = this.topicController
                    .addTopicInfo(new Utf8String(topicName), new Address(topic.getContractAddress()))
                    .get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            List<TopicController.LogAddTopicNameAddressEventResponse> event = TopicController
                    .getLogAddTopicNameAddressEvents(transactionReceipt);

            if (CollectionUtils.isNotEmpty(event)
                    && Web3SDKWrapper.uint256ToInt(event.get(0).retCode) == ErrorCode.TOPIC_ALREADY_EXIST.getCode()) {
                log.info("topic name already exist, {}", topicName);
                throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
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

    public ListPage<String> listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {
        try {
            ListPage<String> listPage = new ListPage<>();
            List<Type> result = this.topicController.listTopicName(Web3SDKWrapper.intToUint256(pageIndex),
                    Web3SDKWrapper.intToUint256(pageSize)).get();
            if (result == null || result.isEmpty()) {
                log.error("TopicController.listTopicName result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            listPage.setPageIndex(pageIndex);
            listPage.setPageSize(pageSize);
            listPage.setTotal(Web3SDKWrapper.uint256ToInt((Uint256) result.get(0)));

            @SuppressWarnings(value = "unchecked")
            DynamicArray<Bytes32> bytes32DynamicArray = (DynamicArray<Bytes32>) result.get(1);
            String[] stringArray = Web3SDKWrapper.bytes32DynamicArrayToStringArrayWithoutTrim(bytes32DynamicArray);
            for (String str : stringArray) {
                if (!StringUtils.isNotEmpty(str)) {
                    log.error("detect topic name is empty, {}", str);
                    continue;
                }

                listPage.getPageData().add(str);
            }

            return listPage;
        } catch (InterruptedException | ExecutionException e) {
            log.error("list topic name failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    public TopicInfo getTopicInfo(String topicName) throws BrokerException {
        try {
            List<Type> typeList = this.topicController.getTopicInfo(new Utf8String(topicName)).get();
            if (typeList == null || typeList.isEmpty()) {
                log.error("TopicController.getTopicInfo result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            String topicAddress = typeList.get(0).toString();
            if (WeEventConstants.ADDRESS_EMPTY.equals(topicAddress)) {
                log.error("TopicController.getTopicInfo address is empty");
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            String senderAddress = typeList.get(1).toString();
            Long createdTimestamp = ((Uint256) typeList.get(2)).getValue().longValue();
            //get topic.sol contract info
            Topic topicData = getTopic(topicName);
            if (topicData == null) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }
            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setTopicName(topicName);
            topicInfo.setTopicAddress(topicAddress);
            topicInfo.setCreatedTimestamp(createdTimestamp);
            topicInfo.setSenderAddress(senderAddress);
            topicInfo.setBlockNumber(topicData.getBlockNumber().get().getValue().longValue());
            topicInfo.setSequenceNumber(topicData.getSequenceNumber().get().getValue().longValue());
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
            TransactionReceipt transactionReceipt = topic.publishWeEvent(new Utf8String(topicName),
                    new Utf8String(eventContent), new Utf8String(extensions)).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            List<Topic.LogWeEventEventResponse> event = Topic.getLogWeEventEvents(transactionReceipt);
            if (CollectionUtils.isNotEmpty(event)) {
                sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, Web3SDKWrapper.uint256ToInt(event.get(0).eventBlockNumer), Web3SDKWrapper.uint256ToInt(event.get(0).eventSeq)));
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
        return Web3SDKWrapper.getBlockHeight(this.web3j);
    }

    /**
     * Fetch all event in target block.
     *
     * @param blockNum the blockNum
     * @return java.lang.Integer null if net error
     */
    public List<WeEvent> loop(Long blockNum) throws BrokerException {
        return Web3SDKWrapper.loop(this.web3j, blockNum);
    }
}
