package com.webank.weevent.core.fisco.web3sdk;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.core.dto.ContractContext;
import com.webank.weevent.core.dto.GroupGeneral;
import com.webank.weevent.core.dto.ListPage;
import com.webank.weevent.core.dto.TbBlock;
import com.webank.weevent.core.dto.TbNode;
import com.webank.weevent.core.dto.TbTransHash;
import com.webank.weevent.core.fisco.AMOPSubscription;
import com.webank.weevent.core.fisco.constant.WeEventConstants;
import com.webank.weevent.core.fisco.util.ParamCheckUtils;
import com.webank.weevent.core.fisco.web3sdk.v2.Web3SDKConnector;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.utils.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Detect FISCO-BCOS version from configuration 'fisco.properties' and then proxy all the invoke to the target.
 * Parameter groupId in all interface:
 * default 1L in 2.x, meanings first group
 * There is 2 different caches for block data. One is local memory, another is redis.
 * All can be opened/closed by configuration. And is independent to each other.
 *
 * @author matthewliu
 * @since 2019/04/28
 */
@Slf4j
public class FiscoBcosDelegate {
    // access to version 2.x
    private final Map<Long, FiscoBcos2> fiscoBcos2Map = new ConcurrentHashMap<>();

    // AMOP subscription
    //private final Map<Long, AMOPSubscription> AMOPSubscriptions = new ConcurrentHashMap<>();

    // binding thread pool
    public ThreadPoolTaskExecutor threadPool;

    // groupId list
    private List<String> groupIdList = new ArrayList<>();

    // fiscoConfig
    private FiscoConfig fiscoConfig;

    private Async asyncHelper;

    /**
     * notify from web3sdk2.x when new block mined
     */
    public interface IBlockEventListener {
        /**
         * @param groupId group id
         * @param blockHeight new block height
         */
        void onEvent(Long groupId, Long blockHeight);
    }

    public void initProxy(FiscoConfig config) throws BrokerException {
        this.fiscoConfig = config;
        this.threadPool = Web3SDKConnector.initThreadPool(config.getWeb3sdkCorePoolSize(),
                config.getWeb3sdkMaxPoolSize(),
                config.getWeb3sdkKeepAliveSeconds());

        if (StringUtils.isBlank(config.getVersion())) {
            log.error("the fisco version in fisco.properties is empty");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
        if (StringUtils.isBlank(config.getNodes())) {
            log.error("the fisco nodes in fisco.properties is null");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }

        if (config.getVersion().startsWith(WeEventConstants.FISCO_BCOS_2_X_VERSION_PREFIX)) {
            log.info("Notice: FISCO-BCOS's version is 2.x");

            // set web3sdk.Async thread pool, special thread for sendAsync
            this.asyncHelper = new Async(threadPool);

            // 1 is always exist
            Long defaultGId = Long.valueOf(WeEvent.DEFAULT_GROUP_ID);
            FiscoBcos2 defaultFiscoBcos2 = new FiscoBcos2(config);
            defaultFiscoBcos2.init(defaultGId);
            this.fiscoBcos2Map.put(defaultGId, defaultFiscoBcos2);
            // this call need default group has been initialized
            List<String> groups = this.listGroupId();

            // init all group in nodes except default one
            groups.remove(WeEvent.DEFAULT_GROUP_ID);
            for (String groupId : groups) {
                Long gid = Long.valueOf(groupId);
                FiscoBcos2 fiscoBcos2 = new FiscoBcos2(config);
                fiscoBcos2.init(gid);
                this.fiscoBcos2Map.put(gid, fiscoBcos2);
            }

            log.info("all group in nodes: {}", this.fiscoBcos2Map.keySet());
        } else {
            log.error("unknown FISCO-BCOS's version");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    public ThreadPoolTaskExecutor getThreadPool() {
        return this.threadPool;
    }

    public boolean supportBlockEventNotify() {
        // 2.0 support notify
        return !this.fiscoBcos2Map.isEmpty();
    }

    /**
     * web3sdk will notify when new block mined in every group.
     *
     * @param listener listener
     */
    public void setListener(@NonNull IBlockEventListener listener) {
        log.info("set IBlockEventListener for every group for FISCO-BCOS 2.x");

        for (Map.Entry<Long, FiscoBcos2> entry : fiscoBcos2Map.entrySet()) {
            entry.getValue().setListener(listener);
        }
    }

    /*
     * list all group id
     *
     * @return list of groupId
     */
    public List<String> listGroupId() throws BrokerException {
        if (this.groupIdList.isEmpty()) {
            // group 1 is always exist
            this.groupIdList = this.fiscoBcos2Map.get(Long.valueOf(WeEvent.DEFAULT_GROUP_ID)).listGroupId();
        }
        return new ArrayList<>(this.groupIdList);
    }

    public boolean createTopic(String topicName, Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).createTopic(topicName);
    }

    public boolean isTopicExist(String topicName, Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).isTopicExist(topicName);
    }

    public ListPage<String> listTopicName(Integer pageIndex, Integer pageSize, Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).listTopicName(pageIndex, pageSize);
    }

    public TopicInfo getTopicInfo(String topicName, Long groupId, boolean skipCache) throws BrokerException {
        Optional<TopicInfo> topicInfo = this.fiscoBcos2Map.get(groupId).getTopicInfo(topicName, skipCache);
        if (!topicInfo.isPresent()) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }
        return topicInfo.get();
    }

    public WeEvent getEvent(String eventId, Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).getEvent(eventId);
    }

    public CompletableFuture<SendResult> publishEvent(String topicName, Long groupId, String eventContent, String extensions) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).publishEvent(topicName, eventContent, extensions);
    }

    public CompletableFuture<SendResult> sendRawTransaction(String topicName, Long groupId, String transactionHex) throws BrokerException {
        ParamCheckUtils.validateTransactionHex(transactionHex);

        return this.fiscoBcos2Map.get(groupId).sendRawTransaction(topicName, transactionHex);
    }

    public Long getBlockHeight(Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).getBlockHeight();
    }

    /**
     * get data from block chain and it's cache
     *
     * @param blockNum block height
     * @param groupId group id
     * @return list of WeEvent
     * @throws BrokerException BrokerException
     */
    public List<WeEvent> loop(Long blockNum, Long groupId) throws BrokerException {
        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        // from block chain
        events = this.fiscoBcos2Map.get(groupId).loop(blockNum);

        return events;
    }

    public GroupGeneral getGroupGeneral(Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).getGroupGeneral();
    }

    public ListPage<TbTransHash> queryTransList(Long groupId, String transHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).queryTransList(transHash, blockNumber, pageIndex, pageSize);
    }

    public ListPage<TbBlock> queryBlockList(Long groupId, String transHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).queryBlockList(transHash, blockNumber, pageIndex, pageSize);
    }

    public ListPage<TbNode> queryNodeList(Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).queryNodeList();
    }

    public FiscoConfig getFiscoConfig() {
        return this.fiscoConfig;
    }

    public ContractContext getContractContext(Long groupId) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).getContractContext();
    }

    public boolean addOperator(Long groupId, String topicName, String operatorAddress) throws BrokerException {
        ParamCheckUtils.validateAddress(operatorAddress);

        return this.fiscoBcos2Map.get(groupId).addOperator(topicName, operatorAddress);
    }

    public boolean delOperator(Long groupId, String topicName, String operatorAddress) throws BrokerException {
        ParamCheckUtils.validateAddress(operatorAddress);

        return this.fiscoBcos2Map.get(groupId).delOperator(topicName, operatorAddress);
    }

    public List<String> listOperator(Long groupId, String topicName) throws BrokerException {
        return this.fiscoBcos2Map.get(groupId).listOperator(topicName);
    }

    public CompletableFuture<SendResult> sendAMOP(String topicName, Long groupId, String content) {
        return this.fiscoBcos2Map.get(groupId).sendAMOP(topicName, content);
    }

    public Map<Long, AMOPSubscription> initAMOP() {
        Map<Long, AMOPSubscription> AMOPSubscriptions = new ConcurrentHashMap<>();

        for (Map.Entry<Long, FiscoBcos2> entry : this.fiscoBcos2Map.entrySet()) {
            log.info("init AMOP for group: {}", entry.getKey());

            AMOPSubscription amopSubscription = new AMOPSubscription(String.valueOf(entry.getKey()), entry.getValue().getService());
            AMOPSubscriptions.put(entry.getKey(), amopSubscription);
        }

        return AMOPSubscriptions;
    }
}
