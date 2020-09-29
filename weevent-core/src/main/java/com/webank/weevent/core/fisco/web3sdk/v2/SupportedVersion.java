package com.webank.weevent.core.fisco.web3sdk.v2;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.client.WeEventPlus;
import com.webank.weevent.core.fisco.util.DataTypeUtils;
import com.webank.weevent.core.fisco.web3sdk.v2.solc10.Topic;
import com.webank.weevent.core.fisco.web3sdk.v2.solc10.TopicController;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple8;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;


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

    public static ImmutablePair<Contract, Contract> loadTopicControlContract(Client client,
                                                                             String controlAddress,
                                                                             int version,
                                                                             int timeout) throws BrokerException {
        // support version list
        switch (version) {
            case 10:
                TopicController topicController = (TopicController) Web3SDK2Wrapper.loadContract(controlAddress, client, TopicController.class);
                String address = "";
                try {
                    address = topicController.getContractAddress();
                } catch (NullPointerException e) {
                    log.error("getTopicAddress failed due to transaction execution error. ", e);
                    throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                }
                Topic topic = (Topic) Web3SDK2Wrapper.loadContract(address, client, Topic.class);

                return new ImmutablePair<>(topicController, topic);

            default:
                log.error("unknown solidity version: {}", version);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }
    }

    private static List<List<TopicInfo>> loadTopicData(Client client, String address, Long version, int timeout) throws BrokerException {
        List<List<TopicInfo>> topicInfos = new ArrayList<>();
        int total;
        switch (version.intValue()) {
            case 10:
                TopicController lowControl = (TopicController) Web3SDK2Wrapper.loadContract(address, client, TopicController.class);
                final int pageSize = 100;
                for (int i = 0; true; i++) {
                    try {
                        Tuple3<BigInteger, BigInteger, List<String>> tuple3 = lowControl.listTopicName(BigInteger.valueOf(i), BigInteger.valueOf(pageSize));

                        total = tuple3.getValue1().intValue();
                        List<TopicInfo> onePage = new ArrayList<>();
                        if (tuple3.getValue2().intValue() > 0) {
                            for (String topicName : tuple3.getValue3()) {
                                TopicInfo topicInfo = new TopicInfo();
                                Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String> result =
                                        lowControl.getTopicInfo(topicName);
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
                        }

                        if (tuple3.getValue2().intValue() < pageSize) {
                            break;
                        }
                    } catch (ContractException e) {
                        log.error("loop block failed due to ExecutionException|NullPointerException|InterruptedException", e);
                        throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
                    }
                }
                break;

            default:
                log.error("unknown solidity version: {}", version);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }

        log.info("topic size: {} in version: {}", total, version);
        return topicInfos;
    }

    private static void saveTopicData(Client client, List<List<TopicInfo>> topicInfos, String address, Long version) throws BrokerException {
        // flush data into high version
        switch (version.intValue()) {
            case 11:
                /*
                TopicController11 highControl = (TopicController11) Web3SDK2Wrapper.loadContract(versions.get(high), web3j, credentials, TopicController11.class);
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
                */
                break;

            default:
                log.error("unknown solidity version: {}", version);
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }
    }

    /*
     * flush topic info from low to high
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @param versions version list
     * @param low low version
     * @param high high version
     * @return true if success
     */
    public static boolean flushData(Client client, Map<Long, String> versions, Long low, Long high, int timeout) throws BrokerException {
        // load data from low version
        List<List<TopicInfo>> topicInfos = loadTopicData(client, versions.get(low), low, timeout);
        saveTopicData(client, topicInfos, versions.get(high), high);
        return true;
    }

    public static WeEvent decodeWeEvent(BigInteger timestamp, TransactionReceipt receipt, int version, Map<String, Contract> historyTopic) {
        try {
            // support version list
            switch (version) {
                case 10:
                    // v10 is com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic
                    Topic topic = (Topic) historyTopic.get(receipt.getTo());
                    Tuple3<String, String, String> input = topic.getPublishWeEventInput(receipt);
                    Tuple1<BigInteger> output = topic.getPublishWeEventOutput(receipt);
                    long seq = output.getValue1().longValue();
                    if (seq <= 0) {
                        log.warn("skip data for invalid sequence: {}", seq);
                        return null;
                    }

                    String topicName = input.getValue1();
                    Map<String, String> extensions = JsonHelper.json2Object(input.getValue3(), new TypeReference<Map<String, String>>() {
                    });
                    if (extensions == null) {
                        extensions = new HashMap<>();
                    }
                    WeEventPlus weEventPlus = new WeEventPlus(timestamp.longValue(), Long.getLong(receipt.getBlockNumber()), receipt.getTransactionHash(), receipt.getFrom());
                    extensions.put(WeEvent.WeEvent_PLUS, JsonHelper.object2Json(weEventPlus));

                    WeEvent event = new WeEvent(topicName, input.getValue2().getBytes(StandardCharsets.UTF_8), extensions);
                    event.setEventId(DataTypeUtils.encodeEventId(topicName, Integer.parseInt(receipt.getBlockNumber()), seq));
                    return event;

                default:
                    log.error("unknown solidity version: {}", version);
                    return null;
            }
        } catch (Exception e) {
            log.warn("decode WeEvent failed, TransactionReceipt: {} {}", receipt.getTransactionHash(), e.getMessage());
            return null;
        }
    }
}
