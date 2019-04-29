package com.webank.weevent.broker.fisco.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.contract.Topic;
import com.webank.weevent.broker.fisco.contract.TopicController;
import com.webank.weevent.broker.fisco.contract.TopicController.LogAddTopicNameAddressEventResponse;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.dto.ResponseData;
import com.webank.weevent.broker.fisco.service.BaseService;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.SerializeUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.channel.dto.EthereumResponse;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.protocol.ObjectMapperFactory;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

@Slf4j
public class TopicServiceImpl extends BaseService {
    private static TopicController topicController;
    private static Map<String, Topic> topicMap;

    public TopicServiceImpl() {
        super();

        if (topicMap == null) {
            topicMap = new ConcurrentHashMap<>();
        }
        if (topicController == null) {
            topicController = (TopicController) getContractService(BrokerApplication.weEventConfig.getTopicControllerAddress(),
                    TopicController.class);
        }
    }

    /**
     * Topic Handler cache.
     *
     * @param topicName the topicName
     * @return Topic null if not exist
     * @throws BrokerException BrokerException
     */
    private Topic getTopic(String topicName) throws BrokerException {
        if (topicMap.containsKey(topicName)) {
            return topicMap.get(topicName);
        }

        if (topicController == null) {
            log.error("topicController is null");
            throw new BrokerException(ErrorCode.TOPIC_CONTROLLER_IS_NULL);
        }

        try {
            Address address = topicController.getTopicAddress(DataTypeUtils.stringToBytes32(topicName)).get();
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
            topicMap.put(topicName, topic);
            return topic;
        } catch (InterruptedException | ExecutionException e) {
            log.error("InterruptedException|ExecutionException raise", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    public ResponseData<Boolean> isTopicExist(String topicName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topicName);

        return new ResponseData<>(getTopic(topicName) != null, ErrorCode.SUCCESS);
    }

    public ResponseData<Boolean> createTopic(String topicName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topicName);

        try {
            // check if topic contract exist
            ResponseData<Boolean> responseData = isTopicExist(topicName);
            if (responseData.getResult()) {
                log.info("topic name already exist, {}", topicName);
                throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
            }

            // deploy topic contract
            Topic topic = Topic.deploy(web3j, credentials, WeEventConstants.GAS_PRICE, WeEventConstants.GAS_LIMIT,
                    WeEventConstants.INILITIAL_VALUE).get();
            if (topic.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after Topic.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            TransactionReceipt transactionReceipt = topicController
                    .addTopicInfo(DataTypeUtils.stringToBytes32(topicName), new Address(topic.getContractAddress()))
                    .get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
            List<LogAddTopicNameAddressEventResponse> event = TopicController
                    .getLogAddTopicNameAddressEvents(transactionReceipt);

            if (CollectionUtils.isNotEmpty(event)) {
                if (DataTypeUtils.uint256ToInt(event.get(0).retCode) == ErrorCode.TOPIC_ALREADY_EXIST.getCode()) {
                    log.info("topic name already exist, {}", topicName);
                    throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
                }
            }
            return new ResponseData<>(true, ErrorCode.SUCCESS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("create topic failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException e) {
            log.error("create topic failed due to transaction timeout. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public ResponseData<ListPage> listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {
        if (pageIndex == null || pageIndex < 0) {
            throw new BrokerException(ErrorCode.TOPIC_PAGE_INDEX_INVALID);
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new BrokerException(ErrorCode.TOPIC_PAGE_SIZE_INVALID);
        }

        try {
            ListPage<String> listPage = new ListPage<>();
            List<Type> result = topicController.listTopicName(DataTypeUtils.intToUint256(pageIndex),
                    DataTypeUtils.intToUint256(pageSize)).get();
            if (result == null || result.isEmpty()) {
                log.error("TopicController.listTopicName result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            listPage.setPageIndex(pageIndex);
            listPage.setPageSize(pageSize);
            listPage.setTotal(DataTypeUtils.uint256ToInt((Uint256) result.get(0)));

            @SuppressWarnings(value = "unchecked")
            DynamicArray<Bytes32> bytes32DynamicArray = (DynamicArray<Bytes32>) result.get(1);
            String[] stringArray = DataTypeUtils.bytes32DynamicArrayToStringArrayWithoutTrim(bytes32DynamicArray);
            List<String> stringList = Arrays.asList(stringArray);
            for (String str : stringList) {
                if (!StringUtils.isNotEmpty(str)) {
                    log.error("detect topic name is empty, {}", str);
                    continue;
                }

                listPage.getPageData().add(str);
            }

            return new ResponseData<>(listPage);
        } catch (InterruptedException | ExecutionException e) {
            log.error("list topic name failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    public ResponseData<TopicInfo> getTopicInfo(String topicName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topicName);

        try {
            List<Type> typeList = topicController.getTopicInfo(DataTypeUtils.stringToBytes32(topicName)).get();
            if (typeList == null || typeList.isEmpty()) {
                log.error("TopicController.getTopicInfo result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            String topicAddress = ((Address) typeList.get(0)).toString();
            if (WeEventConstants.ADDRESS_EMPTY.equals(topicAddress)) {
                log.error("TopicController.getTopicInfo address is empty");
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            String senderAddress = ((Address) typeList.get(1)).toString();
            Long createdTimestamp = ((Uint256) typeList.get(2)).getValue().longValue();

            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setTopicName(topicName);
            topicInfo.setTopicAddress(topicAddress);
            topicInfo.setCreatedTimestamp(createdTimestamp);
            topicInfo.setSenderAddress(senderAddress);
            return new ResponseData<>(topicInfo);

        } catch (InterruptedException | ExecutionException e) {
            log.error("get topic info failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    public void publishEvent(String topicName, String eventContent, IProducer.SendCallBack callBack) throws BrokerException {
        Topic topic = getTopic(topicName);
        if (topic == null) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        SendResult sendResult = new SendResult(SendResult.SendResultStatus.ERROR);
        topic.publishWeEvent(DataTypeUtils.stringToBytes32(topicName), new Utf8String(eventContent),
                new TransactionSucCallback() {
                    @Override
                    public void onResponse(EthereumResponse response) {
                        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                        try {
                            TransactionReceipt transactionReceipt = objectMapper.readValue(response.getContent(),
                                    TransactionReceipt.class);
                            List<Topic.LogWeEventEventResponse> event = Topic.getLogWeEventEvents(transactionReceipt);
                            if (CollectionUtils.isNotEmpty(event)) {
                                sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, DataTypeUtils.uint256ToInt(event.get(0).eventBlockNumer), DataTypeUtils.uint256ToInt(event.get(0).eventSeq)));
                                sendResult.setTopic(DataTypeUtils.bytes32ToString(event.get(0).topicName));
                                if (response.getErrorCode().equals(WeEventConstants.TIME_OUT)) {
                                    sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
                                } else {
                                    sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                                }
                                callBack.onComplete(sendResult);
                            }
                        } catch (Exception e) {
                            callBack.onException(e);
                        }
                    }
                });
    }

    public ResponseData<SendResult> publishEvent(String topicName, String eventContent) throws BrokerException {
        Topic topic = getTopic(topicName);
        if (topic == null) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        try {
            ResponseData<SendResult> responseData = new ResponseData<>();
            SendResult sendResult = new SendResult(SendResult.SendResultStatus.ERROR);

            TransactionReceipt transactionReceipt = topic.publishWeEvent(DataTypeUtils.stringToBytes32(topicName),
                    new Utf8String(eventContent)).get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
            List<Topic.LogWeEventEventResponse> event = Topic.getLogWeEventEvents(transactionReceipt);
            if (CollectionUtils.isNotEmpty(event)) {
                sendResult.setEventId(DataTypeUtils.encodeEventId(topicName, DataTypeUtils.uint256ToInt(event.get(0).eventBlockNumer), DataTypeUtils.uint256ToInt(event.get(0).eventSeq)));
                sendResult.setTopic(DataTypeUtils.bytes32ToString(event.get(0).topicName));
                sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                responseData = new ResponseData<>(sendResult, ErrorCode.SUCCESS);
            }
            return responseData;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("publish event failed due to transaction execution error.", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }


    public ResponseData<WeEvent> getEvent(String eventId) throws BrokerException {
        ParamCheckUtils.validateEventId("",eventId, getBlockHeight());
        Long blockNum = DataTypeUtils.decodeBlockNumber(eventId);
        List<WeEvent> events = this.loop(blockNum);
        for (WeEvent event : events) {
            if (eventId.equals(event.getEventId())) {
                log.info("event:{}", event);
                return new ResponseData<>(event, ErrorCode.SUCCESS);
            }
        }

        throw new BrokerException(ErrorCode.EVENT_ID_NOT_EXIST);
    }
}
