package com.webank.weevent.broker.fisco.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.RedisService;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.contract.Topic;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.LRUCache;
import com.webank.weevent.broker.fisco.util.SerializeUtils;
import com.webank.weevent.broker.fisco.util.Web3sdkUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.DefaultBlockParameterNumber;
import org.bcos.web3j.protocol.core.methods.response.EthBlock;
import org.bcos.web3j.protocol.core.methods.response.EthBlockNumber;
import org.bcos.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;
import org.springframework.boot.SpringApplication;

/**
 * The BaseService for other RPC classes.
 */
@Slf4j
public abstract class BaseService {
    protected static Credentials credentials;
    protected static Web3j web3j;
    protected static RedisService redisService;
    private static LRUCache<String, List<WeEvent>> blockCache;

    /**
     * Load config.
     *
     * @return true, if successful
     */
    public boolean loadConfig() {
        boolean initResult = (initWeb3j() && initCredentials() && initRedisService());
        if (!initResult) {
            System.exit(SpringApplication.exit(BrokerApplication.applicationContext));
        }

        return initResult;
    }

    private boolean initWeb3j() {
        if (web3j == null) {
            web3j = Web3sdkUtils.initWeb3j(BrokerApplication.applicationContext);
        }

        return (web3j != null);
    }

    /**
     * Init the credentials.
     *
     * @return true, if successful
     */
    private boolean initCredentials() {
        if (credentials == null) {
            credentials = Web3sdkUtils.initCredentials(BrokerApplication.applicationContext);
        }

        return (credentials != null);
    }

    private boolean initRedisService() {
        String redisServerIp = BrokerApplication.weEventConfig.getRedisServerIp();
        Integer redisServerPort = BrokerApplication.weEventConfig.getRedisServerPort();
        Integer maxCapacity = BrokerApplication.weEventConfig.getMaxCapacity();

        log.info("begin init redis service");

        if (StringUtils.isNotBlank(redisServerIp) && redisServerPort > 0) {
            redisService = BrokerApplication.applicationContext.getBean(RedisService.class);
            if (blockCache == null) {
                blockCache = new LRUCache<String, List<WeEvent>>(maxCapacity);
            }
        }

        return true;
    }

    /**
     * Gets the web3j.
     *
     * @return the web3j
     */
    public Web3j getWeb3j() {
        if (null == web3j) {
            initWeb3j();
        }

        return web3j;
    }

    private Contract loadContract(String contractAddress, Web3j web3j, Credentials credentials, Class<?> cls) {
        if (null == web3j || null == credentials) {
            if (!loadConfig()) {
                log.error("init web3sdk failed");
                throw new RuntimeException("init web3sdk failed");
            }
        }

        Contract contract = Web3sdkUtils.loadContract(contractAddress, web3j, credentials, cls);
        if (contract == null) {
            String msg = String.format("load contract failed, %s", cls.getSimpleName());
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return contract;
    }

    /**
     * Gets the contract service.
     *
     * @param contractAddress the contract address
     * @param cls the class
     * @return the contract service
     */
    protected Contract getContractService(String contractAddress, Class<?> cls) {
        if (null == web3j || null == credentials) {
            if (!loadConfig()) {
                log.error("init web3sdk failed");
                throw new RuntimeException("init web3sdk failed");
            }
        }

        return loadContract(contractAddress, web3j, credentials, cls);
    }

    /**
     * getBlockHeight
     *
     * @return java.lang.Long 0L if net error
     */
    public Long getBlockHeight() throws BrokerException {
        try {
            EthBlockNumber ethBlockNumber = getWeb3j().ethBlockNumber().sendAsync().get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
            // Web3sdk's rpc return null in "get".
            if (ethBlockNumber == null) {
                return 0L;
            }
            Long blockHeight = ethBlockNumber.getBlockNumber().longValue();
            log.debug("current block height: {}", blockHeight);
            return blockHeight;
        } catch (InterruptedException | ExecutionException | TimeoutException | RuntimeException e) {
            log.error("get block height failed due to InterruptedException|ExecutionException|TimeoutException|RuntimeException", e);
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
        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        //get events from redis server and check if there is any data, if yes, then directly return from redis
        String key = Long.toString(blockNum);
        try {
            if (redisService != null) {
                if (blockCache.containsKey(key)) {
                    return blockCache.get(key);
                } else {
                    if (redisService.isEventsExistInRedis(key)) {
                        events = redisService.readEventsFromRedis(key);
                        return events;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception happened while read events from redis server", e);
        }

        try {
            log.debug("fetch block, blockNum: {}", blockNum);

            // "false" to load only tx hash.
            EthBlock ethBlock = getWeb3j().ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNum), false)
                    .sendAsync().get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
            List<String> transactionHashList = ethBlock.getBlock().getTransactions().stream()
                    .map(transactionResult -> (String) transactionResult.get()).collect(Collectors.toList());
            if (transactionHashList.size() <= 0) {
                return events;
            }
            log.debug("tx in block: {}", transactionHashList.size());

            for (String transactionHash : transactionHashList) {
                EthGetTransactionReceipt transactionReceipt = getWeb3j().ethGetTransactionReceipt(transactionHash)
                        .sendAsync().get(WeEventConstants.TRANSACTION_RECEIPT_TIMEOUT, TimeUnit.SECONDS);
                if (!transactionReceipt.getTransactionReceipt().isPresent()) {
                    log.error(String.format("loop block empty tx receipt, blockNum: %s tx hash: %s", blockNum, transactionHash));
                    return null;
                }

                TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();
                List<Topic.LogWeEventEventResponse> logWeEventEvents = Topic.getLogWeEventEvents(receipt);
                for (Topic.LogWeEventEventResponse logEvent : logWeEventEvents) {
                    String topicName = DataTypeUtils.bytes32ToString(logEvent.topicName);
                    WeEvent event = new WeEvent(topicName, logEvent.eventContent.getValue().getBytes(StandardCharsets.UTF_8));
                    event.setEventId(DataTypeUtils.encodeEventId(topicName,DataTypeUtils.uint256ToInt(logEvent.eventBlockNumer),DataTypeUtils.uint256ToInt(logEvent.eventSeq)));
                    log.debug("get a event from fisco-bcos: {}", event);
                    events.add(event);
                }
            }

            //write events list to redis server
            try {
                if (redisService != null && !events.isEmpty()) {
                    blockCache.putIfAbsent(key, events);
                    redisService.writeEventsToRedis(key, events);
                }
            } catch (Exception e) {
                log.error("Exception happened while write events to redis server", e);
            }

            return events;
        } catch (InterruptedException | ExecutionException | TimeoutException | NullPointerException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error(String.format("loop block failed due to InterruptedException|ExecutionException|TimeoutException|NullPointerException, blockNum: %s", blockNum), e);
            return null;
        } catch (RuntimeException e) {
            log.error("loop block failed due to RuntimeException", e);
            throw new BrokerException("loop block failed due to RuntimeException", e);
        }
    }
}