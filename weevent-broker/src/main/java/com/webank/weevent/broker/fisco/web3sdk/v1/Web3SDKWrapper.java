package com.webank.weevent.broker.fisco.web3sdk.v1;


import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bcos.channel.client.Service;
import org.bcos.channel.handler.ChannelConnections;
import org.bcos.contract.source.ContractAbiMgr;
import org.bcos.contract.source.SystemProxy;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.crypto.GenCredential;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.bcos.web3j.protocol.core.DefaultBlockParameterNumber;
import org.bcos.web3j.protocol.core.methods.response.EthBlock;
import org.bcos.web3j.protocol.core.methods.response.EthBlockNumber;
import org.bcos.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;

/**
 * Wrapper of Web3SDK 1.x function.
 * This class can run without spring's ApplicationContext.
 * CNS's API in FISCO-BCOS 1.3 DO NOT SUPPORT versioned address, so can not upgrade solidity gracefully.
 *
 * @author matthewliu
 * @since 2019/04/22
 */
@Slf4j
public class Web3SDKWrapper {
    // topic control address in CNS
    public final static String WeEventTopicControlAddress = "WeEvent_topic_control_address";

    /**
     * init web3j handler
     *
     * @return Web3j
     */
    public static Web3j initWeb3j(FiscoConfig fiscoConfig) throws BrokerException {
        // init web3j with given group id
        try {
            log.info("begin to initialize web3sdk");

            int web3sdkTimeout = fiscoConfig.getWeb3sdkTimeout();

            Service service = new Service();
            // group info
            service.setOrgID(fiscoConfig.getOrgId());
            service.setConnectSeconds(web3sdkTimeout / 1000);
            // reconnect idle time 100ms
            service.setConnectSleepPerMillis(100);

            // connect key and string
            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setCaCertPath("classpath:" + fiscoConfig.getV1CaCrtPath());
            channelConnections.setClientCertPassWord(fiscoConfig.getV1ClientCrtPassword());
            channelConnections.setClientKeystorePath("classpath:" + fiscoConfig.getV1ClientKeyStorePath());
            channelConnections.setKeystorePassWord(fiscoConfig.getV1KeyStorePassword());
            List<String> nodeList = Arrays.asList(fiscoConfig.getNodes().split(";"));
            for (int i = 0; i < nodeList.size(); i++) {
                nodeList.set(i, fiscoConfig.getOrgId() + "@" + nodeList.get(i));
            }
            channelConnections.setConnectionsStr(nodeList);
            ConcurrentHashMap<String, ChannelConnections> keyID2connections = new ConcurrentHashMap<>();
            keyID2connections.put(fiscoConfig.getOrgId(), channelConnections);
            service.setAllChannelConnections(keyID2connections);

            // special thread for TransactionSucCallback.onResponse, callback from IO thread directly if not setting
            //service.setThreadPool(poolTaskExecutor);
            service.run();

            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setChannelService(service);
            channelEthereumService.setTimeout(web3sdkTimeout);
            Web3j web3j = Web3j.build(channelEthereumService);

            // check connect with Web3ClientVersion command
            String nodeVersion = web3j.web3ClientVersion().sendAsync()
                    .get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).getWeb3ClientVersion();
            if (StringUtils.isBlank(nodeVersion)
                    || !nodeVersion.contains(WeEventConstants.FISCO_BCOS_1_X_VERSION_PREFIX)) {
                log.error("init web3sdk failed, mismatch FISCO-BCOS version in node: {}", nodeVersion);
                throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
            }

            log.info("initialize web3sdk success");
            return web3j;
        } catch (Exception e) {
            log.error("init web3sdk failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    /**
     * get account Credentials
     *
     * @return Credentials return null if error
     */
    public static Credentials getCredentials(FiscoConfig fiscoConfig) {
        log.debug("begin init Credentials");

        Credentials credentials = GenCredential.create(fiscoConfig.getAccount());
        if (null == credentials) {
            log.error("init Credentials failed");
            return null;
        }

        log.info("init Credentials success");
        return credentials;
    }

    /**
     * load contract handler
     *
     * @param contractAddress contractAddress
     * @param web3j web3j
     * @param credentials credentials
     * @param cls contract java class
     * @return Contract return null if error
     */
    public static Contract loadContract(String contractAddress, Web3j web3j, Credentials credentials, Class<?> cls) throws BrokerException {
        log.info("begin load contract, {}", cls.getSimpleName());

        try {
            // load contract
            Method method = cls.getMethod("load",
                    String.class,
                    Web3j.class,
                    Credentials.class,
                    BigInteger.class,
                    BigInteger.class);

            Object contract = method.invoke(null,
                    contractAddress,
                    web3j,
                    credentials,
                    WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT);

            if (contract != null) {
                log.info("load contract success, {}", cls.getSimpleName());
                return (Contract) contract;
            }

            log.info("load contract failed, {}", cls.getSimpleName());
        } catch (Exception e) {
            log.error(String.format("load contract[%s] failed", cls.getSimpleName()), e);
        }

        throw new BrokerException(ErrorCode.LOAD_CONTRACT_ERROR);
    }

    /**
     * deploy topic control into web3j
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @return contract
     * @throws BrokerException BrokerException
     */
    public static String deployTopicControl(Web3j web3j, Credentials credentials) throws BrokerException {
        log.info("begin deploy topic control");

        try {
            Future<TopicData> f1 = TopicData.deploy(web3j, credentials, WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT, WeEventConstants.INITIAL_VALUE);
            TopicData topicData = f1.get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

            log.info("topic data contract address: {}", topicData.getContractAddress());
            if (topicData.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicData.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            Future<TopicController> f2 = TopicController.deploy(web3j, credentials, WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT, WeEventConstants.INITIAL_VALUE, new Address(topicData.getContractAddress()));
            TopicController topicController = f2.get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);

            log.info("topic control contract address: {}", topicController.getContractAddress());
            if (topicController.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicController.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            log.info("deploy topic control success");
            return topicController.getContractAddress();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("deploy contract failed", e);
            throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
        }
    }

    /**
     * get ContractAbiMgr address from system proxy
     *
     * @return address
     */
    public static String getContractAbiMgr(Web3j web3j, Credentials credentials, String proxyAddress) throws BrokerException {
        SystemProxy systemProxy = (SystemProxy) loadContract(proxyAddress, web3j, credentials, SystemProxy.class);
        try {
            Future<List<Type>> f = systemProxy.getRoute(new Utf8String("ContractAbiMgr"));
            List<Type> route = f.get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            return route.get(0).toString();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            log.error("get ContractAbiMgr address failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    /**
     * get address from CNS.
     * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/features/CNS/README.html
     *
     * @return address
     */
    public static String getAddress(Web3j web3j, Credentials credentials, String proxyAddress) throws BrokerException {
        String CNSAddress = getContractAbiMgr(web3j, credentials, proxyAddress);
        log.info("CNS address in system proxy: {}", CNSAddress);
        if (StringUtils.isBlank(CNSAddress)) {
            return "";
        }

        try {
            ContractAbiMgr abiMgr = (ContractAbiMgr) loadContract(CNSAddress, web3j, credentials, ContractAbiMgr.class);
            Future<Address> f = abiMgr.getAddr(new Utf8String(WeEventTopicControlAddress));
            String address = f.get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS).toString();
            if (StringUtils.isBlank(address) || WeEventConstants.ADDRESS_EMPTY.equals(address)) {
                return "";
            }

            log.info("topic control address in CNS: {}", address);
            return address;
        } catch (ExecutionException | TimeoutException | InterruptedException | NullPointerException e) {
            log.error("load topic control address from CNS failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    /**
     * add address in CNS, failed if add again
     *
     * @param proxyAddress system proxy address
     * @param address address
     * @return boolean true if success
     */
    public static boolean addAddress(Web3j web3j, Credentials credentials, String proxyAddress, String address) throws BrokerException {
        String CNSAddress = getContractAbiMgr(web3j, credentials, proxyAddress);
        log.info("CNS address in system proxy: {}", CNSAddress);
        if (StringUtils.isBlank(CNSAddress)) {
            return false;
        }

        ContractAbiMgr abiMgr = (ContractAbiMgr) loadContract(CNSAddress, web3j, credentials, ContractAbiMgr.class);
        log.info("add topic control address into CNS: {}", address);
        try {
            Future<TransactionReceipt> f = abiMgr.addAbi(
                    new Utf8String(WeEventTopicControlAddress),
                    new Utf8String("TopicController"), new Utf8String("1.0"),
                    new Utf8String(TopicController.ABI), new Address(address));
            TransactionReceipt transactionReceipt = f.get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            List<ContractAbiMgr.AddAbiEventResponse> resp = ContractAbiMgr.getAddAbiEvents(transactionReceipt);
            if (resp.isEmpty()) {
                log.error("add topic control address into CNS failed");
                throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
            }
            return true;
        } catch (ExecutionException | TimeoutException | InterruptedException | NullPointerException e) {
            log.error("add topic control address into CNS failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    /**
     * getBlockHeight
     *
     * @return 0L if net error
     */
    public static Long getBlockHeight(Web3j web3j) throws BrokerException {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            // Web3sdk's rpc return null in "get".
            if (blockNumber == null) {
                return 0L;
            }
            Long blockHeight = blockNumber.getBlockNumber().longValue();
            log.debug("current block height: {}", blockHeight);
            return blockHeight;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("get block height failed due to InterruptedException|ExecutionException|TimeoutException", e);
            throw new BrokerException(ErrorCode.GET_BLOCK_HEIGHT_ERROR);
        }
    }

    /**
     * Fetch all event in target block.
     *
     * @param blockNum the blockNum
     * @return null if net error
     */
    public static List<WeEvent> loop(Web3j web3j, Long blockNum) throws BrokerException {
        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        try {
            log.debug("fetch block, blockNum: {}", blockNum);

            // "false" to load only tx hash.
            EthBlock ethBlock = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNum), false)
                    .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            List<String> transactionHashList = ethBlock.getBlock().getTransactions().stream()
                    .map(transactionResult -> (String) transactionResult.get()).collect(Collectors.toList());
            if (transactionHashList.size() <= 0) {
                return events;
            }
            log.debug("tx in block: {}", transactionHashList.size());

            for (String transactionHash : transactionHashList) {
                EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash)
                        .sendAsync().get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                if (!transactionReceipt.getTransactionReceipt().isPresent()) {
                    log.error(String.format("loop block empty tx receipt, blockNum: %s tx hash: %s", blockNum, transactionHash));
                    return null;
                }

                TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();
                List<Topic.LogWeEventEventResponse> logWeEventEvents = Topic.getLogWeEventEvents(receipt);
                for (Topic.LogWeEventEventResponse logEvent : logWeEventEvents) {
                    String topicName = logEvent.topicName.toString();

                    WeEvent event = new WeEvent(topicName,
                            logEvent.eventContent.getValue().getBytes(StandardCharsets.UTF_8),
                            DataTypeUtils.json2Map(logEvent.extensions.toString()));
                    event.setEventId(DataTypeUtils.encodeEventId(topicName, uint256ToInt(logEvent.eventBlockNumer), uint256ToInt(logEvent.eventSeq)));

                    log.debug("get a event from block chain: {}", event);
                    events.add(event);
                }
            }

            return events;
        } catch (ExecutionException | TimeoutException | NullPointerException | InterruptedException e) { // Web3sdk's rpc return null
            // Web3sdk send async will arise InterruptedException
            log.error("loop block failed due to ExecutionException|TimeoutException|NullPointerException|InterruptedException", e);
            throw new BrokerException(ErrorCode.WEB3SDK_RPC_ERROR);
        }
    }

    /**
     * String to bytes 32.
     *
     * @param string the string
     * @return the bytes 32
     */
    public static Bytes32 stringToBytes32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return new Bytes32(byteValueLen32);
    }

    /**
     * Uint 256 to int.
     *
     * @param value the value
     * @return the int
     */
    public static int uint256ToInt(Uint256 value) {
        return value.getValue().intValue();
    }


    /**
     * Convert a Byte32 data to Java String. IMPORTANT NOTE: Byte to String is not 1:1 mapped. So -
     * Know your data BEFORE do the actual transform! For example, Deximal Bytes, or ASCII Bytes are
     * OK to be in Java String, but Encrypted Data, or raw Signature, are NOT OK.
     *
     * @param bytes32 the bytes 32
     * @return String
     */
    public static String bytes32ToString(Bytes32 bytes32) {
        byte[] strs = bytes32.getValue();
        String str = new String(strs);
        return str.trim();
    }

    /**
     * Long to int 256.
     *
     * @param value the value
     * @return the int 256
     */
    public static Uint256 longToUint256(long value) {
        return new Uint256(value);
    }

    /**
     * Int to Uint 256.
     *
     * @param value the value
     * @return the Uint 256
     */
    public static Uint256 intToUint256(int value) {
        return new Uint256(value);
    }

    /**
     * Bytes 32 dynamic array to string array
     *
     * @param bytes32DynamicArray the bytes 32 dynamic array
     * @return the string[]
     */
    public static String[] bytes32DynamicArrayToStringArrayWithoutTrim(
            DynamicArray<Bytes32> bytes32DynamicArray) {
        List<Bytes32> bytes32List = bytes32DynamicArray.getValue();
        String[] stringArray = new String[bytes32List.size()];
        for (int i = 0; i < bytes32List.size(); i++) {
            stringArray[i] = bytes32ToString(bytes32List.get(i));
        }
        return stringArray;
    }
}

