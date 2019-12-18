package com.webank.weevent.broker.fisco.web3sdk;


import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.v2.SupportedVersion;
import com.webank.weevent.broker.fisco.web3sdk.v2.Web3SDK2Wrapper;
import com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic;
import com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple8;
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

    // tx account
    private Credentials credentials;

    // real handler
    private Web3j web3j;

    // topic control contract in nowSupport
    private TopicController topicController;

    // topic contract in nowSupport
    private Topic topic;

    // topic info list in local memory, some fields may be expired
    private Map<String, TopicInfo> topicInfo = new ConcurrentHashMap<>();

    // history topic, (address <-> Contract)
    private Map<String, Contract> historyTopicContract = new ConcurrentHashMap<>();
    // history topic, (address <-> version)
    private Map<String, Long> historyTopicVersion = new ConcurrentHashMap<>();

    public FiscoBcos2(FiscoConfig fiscoConfig) {
        this.fiscoConfig = fiscoConfig;
    }

    public void init(Long groupId) throws BrokerException {
        log.info("WeEvent support solidity version, now: {} support: {}", SupportedVersion.nowVersion, SupportedVersion.history);

        if (this.topicController == null) {
            this.credentials = Web3SDK2Wrapper.getCredentials(this.fiscoConfig);
            this.web3j = Web3SDK2Wrapper.initWeb3j(groupId, this.fiscoConfig);

            Map<Long, String> addresses = Web3SDK2Wrapper.listAddress(this.web3j, this.credentials);
            log.info("address list in CRUD: {}", addresses);

            if (addresses.isEmpty() || !addresses.containsKey(SupportedVersion.nowVersion)) {
                log.error("no topic control[nowVersion: {}] address in CRUD, please deploy it first", SupportedVersion.nowVersion);
                throw new BrokerException(ErrorCode.TOPIC_CONTROLLER_IS_NULL);
            }

            for (Map.Entry<Long, String> controlAddress : addresses.entrySet()) {
                log.info("init topic control {} -> {}", controlAddress.getKey(), controlAddress.getValue());

                ImmutablePair<Contract, Contract> contracts = SupportedVersion.loadTopicControlContract(
                        this.web3j, this.credentials, controlAddress.getValue(), controlAddress.getKey().intValue());
                this.historyTopicContract.put(contracts.right.getContractAddress(), contracts.right);
                this.historyTopicVersion.put(contracts.right.getContractAddress(), controlAddress.getKey());

                // publish and admin function use the nowVersion
                if (controlAddress.getKey().equals(SupportedVersion.nowVersion)) {
                    log.info("detect topic control in now version: {}", SupportedVersion.nowVersion);

                    this.topicController = (TopicController) contracts.left;
                    this.topic = (Topic) contracts.right;
                }
            }

            log.info("all supported solidity version: {}", this.historyTopicVersion);
        }
    }

    public void setListener(FiscoBcosDelegate.IBlockEventListener listener) {
        Web3SDK2Wrapper.setBlockNotifyCallBack(this.web3j, listener);
    }

    public List<String> listGroupId() throws BrokerException {
        return Web3SDK2Wrapper.listGroupId(this.web3j);
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
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }

        if (StringUtils.isBlank(contractAddress)) {
            String msg = "load contract failed, " + cls.getSimpleName();
            log.error(msg);
            throw new BrokerException(ErrorCode.LOAD_CONTRACT_ERROR);
        }

        return Web3SDK2Wrapper.loadContract(contractAddress, this.web3j, this.credentials, cls);
    }

    public boolean isTopicExist(String topicName) throws BrokerException {
        try {
            getTopicInfo(topicName, false);
            return true;
        } catch (BrokerException e) {
            if (e.getCode() == ErrorCode.TOPIC_NOT_EXIST.getCode()) {
                return false;
            }
            throw e;
        }
    }

    public boolean createTopic(String topicName) throws BrokerException {
        try {
            // check if topic contract exist
            if (isTopicExist(topicName)) {
                log.info("topic name already exist, {}", topicName);
                throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
            }

            TransactionReceipt transactionReceipt = this.topicController.addTopicInfo(topicName)
                    .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            if (!transactionReceipt.isStatusOK()) {
                log.error("addTopicInfo failed due to transaction execution error");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }

            Boolean result = this.topicController.getAddTopicInfoOutput(transactionReceipt).getValue1();
            if (!result) {
                log.info("topic name already exist, {}", topicName);
                throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
            }

            return true;
        } catch (InterruptedException | ExecutionException e) {
            log.error("addTopicInfo failed due to transaction execution error. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException e) {
            log.error("addTopicInfo failed due to transaction timeout. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public ListPage<String> listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {
        try {
            ListPage<String> listPage = new ListPage<>();
            Tuple3<BigInteger, BigInteger, List<String>> result = this.topicController.listTopicName(BigInteger.valueOf(pageIndex),
                    BigInteger.valueOf(pageSize)).sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            if (result == null) {
                log.error("TopicController.listTopicName result is empty");
                throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
            }
            listPage.setPageIndex(pageIndex);
            listPage.setTotal(result.getValue1().intValue());
            listPage.setPageSize(result.getValue2().intValue());
            listPage.setPageData(result.getValue3());
            return listPage;
        } catch (InterruptedException | ExecutionException e) {
            log.error("listTopicName failed due to web3sdk rpc error.", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        } catch (TimeoutException e) {
            log.error("listTopicName failed due to web3sdk rpc timeout. ", e);
            throw new BrokerException(ErrorCode.TRANSACTION_TIMEOUT);
        }
    }

    public TopicInfo getTopicInfo(String topicName, boolean skipCache) throws BrokerException {
        if (!skipCache && this.topicInfo.containsKey(topicName)) {
            return this.topicInfo.get(topicName);
        }

        try {
            Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String> topic =
                    this.topicController.getTopicInfo(topicName).sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            if (topic == null) {
                log.error("TopicController.getTopicInfo result is empty");
                throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
            }

            if (!topic.getValue1()) {
                log.info("topic not exist, {}", topicName);
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setTopicName(topicName);
            topicInfo.setSenderAddress(topic.getValue2());
            topicInfo.setCreatedTimestamp(topic.getValue3().longValue());
            topicInfo.setSequenceNumber(topic.getValue5().longValue());
            topicInfo.setBlockNumber(topic.getValue6().longValue());
            topicInfo.setLastTimestamp(topic.getValue7().longValue());

            this.topicInfo.put(topicName, topicInfo);
            return topicInfo;
        } catch (InterruptedException | ExecutionException e) {
            log.error("getTopicInfo failed due to web3sdk rpc error.", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        } catch (TimeoutException e) {
            log.error("getTopicInfo failed due to web3sdk rpc timeout.", e);
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

    static class WeEventTransactionCallback extends TransactionSucCallback {
        private String topicName;
        private Topic topic;

        public CompletableFuture<SendResult> future = new CompletableFuture<>();

        WeEventTransactionCallback(String topicName, Topic topic) {
            this.topicName = topicName;
            this.topic = topic;
        }

        @Override
        public void onResponse(TransactionReceipt receipt) {
            SendResult sendResult = new SendResult();
            sendResult.setTopic(this.topicName);

            // success
            if (receipt.isStatusOK()) {
                Tuple1<BigInteger> result = this.topic.getPublishWeEventOutput(receipt);
                sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                sendResult.setEventId(DataTypeUtils.encodeEventId(this.topicName,
                        receipt.getBlockNumber().intValue(),
                        result.getValue1().intValue()));
            } else { // error
                if ("Transaction receipt timeout.".equals(receipt.getStatus())) {
                    log.error("publish event failed due to transaction execution timeout. {}",
                            StatusCode.getStatusMessage(receipt.getStatus(), receipt.getMessage()));
                    sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
                } else {
                    log.error("publish event failed due to transaction execution error. {}",
                            StatusCode.getStatusMessage(receipt.getStatus(), receipt.getMessage()));
                    sendResult.setStatus(SendResult.SendResultStatus.ERROR);
                }
            }

            log.info("publish result: {}", sendResult);
            this.future.complete(sendResult);
        }
    }

    public CompletableFuture<SendResult> publishEvent(String topicName, String eventContent, String extensions) throws BrokerException {
        if (!isTopicExist(topicName)) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        log.info("publish async...");
        WeEventTransactionCallback weEventTransactionCallback = new WeEventTransactionCallback(topicName, this.topic);
        this.topic.publishWeEvent(topicName, eventContent, extensions, weEventTransactionCallback);
        return weEventTransactionCallback.future;
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
        return Web3SDK2Wrapper.loop(this.web3j, blockNum, this.historyTopicVersion, this.historyTopicContract);
    }

    public GroupGeneral getGroupGeneral() throws BrokerException {
        return Web3SDK2Wrapper.getGroupGeneral(this.web3j);
    }

    public ListPage<TbTransHash> queryTransList(String transHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        return Web3SDK2Wrapper.queryTransList(this.web3j, transHash, blockNumber, pageIndex, pageSize);
    }

    public ListPage<TbBlock> queryBlockList(String transHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        return Web3SDK2Wrapper.queryBlockList(this.web3j, transHash, blockNumber, pageIndex, pageSize);
    }

    public ListPage<TbNode> queryNodeList() throws BrokerException {
        return Web3SDK2Wrapper.queryNodeList(this.web3j);
    }
}
