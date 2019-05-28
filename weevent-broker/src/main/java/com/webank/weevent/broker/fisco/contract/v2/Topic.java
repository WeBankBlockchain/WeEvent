package com.webank.weevent.broker.fisco.contract.v2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.EventEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Event;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.request.BcosFilter;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.TransactionManager;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("unchecked")
public class Topic extends Contract {
    private static final String BINARY = "6080604052600160005534801561001557600080fd5b50610321806100256000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063a99077f214610046575b600080fd5b34801561005257600080fd5b50610139600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610153565b604051808215151515815260200191505060405180910390f35b60007f1c0e3e64fa3c39ea839f697c66d1bc2b3d10be51172d570c18012b860e5a5832846000808154809291906001019190505543868660405180806020018681526020018581526020018060200180602001848103845289818151815260200191508051906020019080838360005b838110156101de5780820151818401526020810190506101c3565b50505050905090810190601f16801561020b5780820380516001836020036101000a031916815260200191505b50848103835286818151815260200191508051906020019080838360005b83811015610244578082015181840152602081019050610229565b50505050905090810190601f1680156102715780820380516001836020036101000a031916815260200191505b50848103825285818151815260200191508051906020019080838360005b838110156102aa57808201518184015260208101905061028f565b50505050905090810190601f1680156102d75780820380516001836020036101000a031916815260200191505b509850505050505050505060405180910390a16001905093925050505600a165627a7a723058209c923536ca7fc1e28b32691ceb8dad60649c8e9100bea51e3bec1e243dabac180029";

    public static final String FUNC_PUBLISHWEEVENT = "publishWeEvent";

    public static final Event LOGWEEVENT_EVENT = new Event("LogWeEvent",
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Utf8String>() {
            }, new TypeReference<Utf8String>() {
            }));
    ;

    @Deprecated
    protected Topic(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Topic(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Topic(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Topic(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> publishWeEvent(String topicName, String eventContent, String extensions) {
        final Function function = new Function(
                FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(eventContent),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(extensions)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void publishWeEvent(String topicName, String eventContent, String extensions, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(eventContent),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(extensions)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public List<LogWeEventEventResponse> getLogWeEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(LOGWEEVENT_EVENT, transactionReceipt);
        ArrayList<LogWeEventEventResponse> responses = new ArrayList<LogWeEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            LogWeEventEventResponse typedResponse = new LogWeEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.topicName = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.eventSeq = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.eventBlockNumer = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.eventContent = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.extensions = (String) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<LogWeEventEventResponse> logWeEventEventFlowable(BcosFilter filter) {
        return web3j.logFlowable(filter).map(new io.reactivex.functions.Function<Log, LogWeEventEventResponse>() {
            @Override
            public LogWeEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(LOGWEEVENT_EVENT, log);
                LogWeEventEventResponse typedResponse = new LogWeEventEventResponse();
                typedResponse.log = log;
                typedResponse.topicName = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.eventSeq = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.eventBlockNumer = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.eventContent = (String) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.extensions = (String) eventValues.getNonIndexedValues().get(4).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<LogWeEventEventResponse> logWeEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        BcosFilter filter = new BcosFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(LOGWEEVENT_EVENT));
        return logWeEventEventFlowable(filter);
    }

    @Deprecated
    public static Topic load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Topic load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Topic load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Topic(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Topic load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Topic(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Topic> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Topic.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Topic> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Topic.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Topic> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Topic.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Topic> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Topic.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class LogWeEventEventResponse {
        public Log log;

        public String topicName;

        public BigInteger eventSeq;

        public BigInteger eventBlockNumer;

        public String eventContent;

        public String extensions;
    }
}
