package com.webank.weevent.broker.fisco.web3sdk.v2;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcos2;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple8;
import org.fisco.bcos.web3j.tx.Contract;

/**
 * all supported solidity version.
 *
 * @author matthewliu
 * @since 2019/09/27
 */
@Slf4j
public class SupportedVersion {
    public static final List<Long> history = Arrays.asList(10L);
    public static final Long nowVersion = 10L;

    public static ImmutablePair<Contract, Contract> loadTopicControlContract(Web3j web3j,
                                                                             Credentials credentials,
                                                                             String controlAddress,
                                                                             int version) throws BrokerException {
        // support version list
        switch (version) {
            case 10:
                com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController topicController =
                        (com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController)
                                Web3SDK2Wrapper.loadContract(controlAddress, web3j, credentials,
                                        com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController.class);
                String address = "";
                try {
                    address = topicController.getTopicAddress().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | NullPointerException e) {
                    log.error("getTopicAddress failed due to transaction execution error. ", e);
                    throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                } catch (TimeoutException e) {
                    log.error("getTopicAddress failed due to transaction timeout. ", e);
                    throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                }
                com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic topic =
                        (com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic)
                                Web3SDK2Wrapper.loadContract(address, web3j, credentials, com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic.class);

                return new ImmutablePair<>(topicController, topic);

            default:
                log.error("unknown solidity version: {}", version);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }
    }

    /**
     * flush topic info from low to high
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @param versions version list
     * @param low low version
     * @param high high version
     * @return true if success
     */
    public static boolean flushData(Web3j web3j, Credentials credentials, Map<Long, String> versions, Long low, Long high) throws BrokerException {
        List<List<TopicInfo>> topicInfos = new ArrayList<>();

        int total = 0;
        // load data from low version
        switch (low.intValue()) {
            case 10:
                com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController lowControl =
                        (com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController)
                                Web3SDK2Wrapper.loadContract(versions.get(low), web3j, credentials,
                                        com.webank.weevent.broker.fisco.web3sdk.v2.solc10.TopicController.class);
                if (lowControl == null) {
                    String msg = "load topic control contract failed, version: " + low;
                    log.error(msg);
                    throw new BrokerException(ErrorCode.LOAD_CONTRACT_ERROR);
                }

                final int pageSize = 100;
                for (int i = 0; true; i++) {
                    try {
                        Tuple3<BigInteger, BigInteger, List<String>> tuple3 = lowControl.listTopicName(BigInteger.valueOf(i), BigInteger.valueOf(pageSize))
                                .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

                        total = tuple3.getValue1().intValue();
                        List<TopicInfo> onePage = new ArrayList<>();
                        for (String topicName : tuple3.getValue3()) {
                            TopicInfo topicInfo = new TopicInfo();
                            Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String> result =
                                    lowControl.getTopicInfo(topicName).sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                            if (result.getValue1()) {
                                //topic
                                topicInfo.setTopicName(topicName);
                                topicInfo.setSenderAddress(result.getValue2());
                                topicInfo.setCreatedTimestamp(result.getValue3().longValue());
                                topicInfo.setBlockNumber(result.getValue4().longValue());

                                // last event
                                topicInfo.setSequenceNumber(result.getValue5().longValue());
                                topicInfo.setLastBlock(result.getValue6().longValue());
                                topicInfo.setLastTimestamp(result.getValue7().longValue());
                                topicInfo.setLastSender(result.getValue8());

                                onePage.add(topicInfo);
                            }
                        }

                        topicInfos.add(onePage);

                        if (tuple3.getValue2().intValue() < pageSize) {
                            break;
                        }
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        log.error("loop block failed due to ExecutionException|NullPointerException|InterruptedException", e);
                        throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                    }
                }
                break;

            default:
                log.error("unknown solidity version: {}", low);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }

        log.info("topic size: {} in version: {}", total, low);
        // flush data into high version
        switch (high.intValue()) {
            case 11:
                com.webank.weevent.broker.fisco.web3sdk.v2.solc11.TopicController highControl =
                        (com.webank.weevent.broker.fisco.web3sdk.v2.solc11.TopicController)
                                Web3SDK2Wrapper.loadContract(versions.get(high), web3j, credentials, com.webank.weevent.broker.fisco.web3sdk.v2.solc11.TopicController.class);
                if (highControl == null) {
                    String msg = "load topic control contract failed, version: " + high;
                    log.error(msg);
                    throw new BrokerException(ErrorCode.LOAD_CONTRACT_ERROR);
                }

                for (List<TopicInfo> onePage : topicInfos) {
                    try {
                        List<String> topicName = new ArrayList<>();
                        List<String> topicSender = new ArrayList<>();
                        List<BigInteger> topicTimestamp = new ArrayList<>();
                        List<BigInteger> topicBlock = new ArrayList<>();
                        List<BigInteger> lastSequence = new ArrayList<>();
                        List<BigInteger> lastBlock = new ArrayList<>();
                        List<BigInteger> lastTimestamp = new ArrayList<>();
                        List<String> lastSender = new ArrayList<>();
                        for (TopicInfo topicInfo : onePage) {
                            topicName.add(topicInfo.getTopicName());
                            topicSender.add(topicInfo.getSenderAddress());
                            topicTimestamp.add(BigInteger.valueOf(topicInfo.getCreatedTimestamp()));
                            topicBlock.add(BigInteger.valueOf(topicInfo.getBlockNumber()));
                            lastSequence.add(BigInteger.valueOf(topicInfo.getSequenceNumber()));
                            lastBlock.add(BigInteger.valueOf(topicInfo.getLastBlock()));
                            lastTimestamp.add(BigInteger.valueOf(topicInfo.getLastTimestamp()));
                            lastSender.add(topicInfo.getLastSender());
                        }
                        TransactionReceipt receipt = highControl.flushTopicInfo(topicName, topicSender, topicTimestamp, topicBlock,
                                lastSequence, lastBlock, lastTimestamp, lastSender)
                                .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                        if (!receipt.isStatusOK()) {
                            log.error("flushTopicInfo failed");
                            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                        }
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        log.error("loop block failed due to ExecutionException|NullPointerException|InterruptedException", e);
                        throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                    }
                }
                break;

            default:
                log.error("unknown solidity version: {}", high);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }

        return true;
    }

    public static WeEvent decodeWeEvent(TransactionReceipt receipt, int version) throws BrokerException {
        // support version list
        switch (version) {
            case 10:
                // v10 is com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic
                com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic topic = (com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic)
                        FiscoBcos2.getHistoryTopicContract().get(receipt.getContractAddress());

                Tuple3<String, String, String> input = topic.getPublishWeEventInput(receipt);
                Tuple1<BigInteger> output = topic.getPublishWeEventOutput(receipt);

                String topicName = input.getValue1();
                WeEvent event = new WeEvent(topicName,
                        input.getValue2().getBytes(StandardCharsets.UTF_8),
                        DataTypeUtils.json2Map(input.getValue3()));
                event.setEventId(DataTypeUtils.encodeEventId(topicName,
                        receipt.getBlockNumber().intValue(),
                        output.getValue1().intValue()));

                return event;

            default:
                log.error("unknown solidity version: {}", version);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }
    }
}
