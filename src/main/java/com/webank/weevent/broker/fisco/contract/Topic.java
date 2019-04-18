package com.webank.weevent.broker.fisco.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.web3j.abi.EventEncoder;
import org.bcos.web3j.abi.EventValues;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Event;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.bcos.web3j.protocol.core.methods.request.EthFilter;
import org.bcos.web3j.protocol.core.methods.response.Log;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;
import org.bcos.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.bcos.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 * <p>Generated with web3j version none.
 */
public final class Topic extends Contract {
    private static String BINARY = "60606040526001600055341561001157fe5b5b6101cd806100216000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063635a47ff1461003b575bfe5b341561004357fe5b6100a060048080356000191690602001909190803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506100ba565b604051808215151515815260200191505060405180910390f35b60007fad6f1fe737d306b775970e326922a0deda069ac7d50a095a2a39453f6085da7883600060008154809291906001019190505543856040518085600019166000191681526020018481526020018381526020018060200182810382528381815181526020019150805190602001908083836000831461015a575b80518252602083111561015a57602082019150602081019050602083039250610136565b505050905090810190601f1680156101865780820380516001836020036101000a031916815260200191505b509550505050505060405180910390a1600190505b929150505600a165627a7a72305820cab22be36e6cd0cb22ed7a2aaaf227e23e8ca869ee611b3f8e6e00aeece60abc0029";

    public static final String ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"},{\"name\":\"eventContent\",\"type\":\"string\"}],\"name\":\"publishWeEvent\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"topicName\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"eventSeq\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"eventBlockNumer\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"eventContent\",\"type\":\"string\"}],\"name\":\"LogWeEvent\",\"type\":\"event\"}]";

    private Topic(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit, isInitByName);
    }

    private Topic(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit, isInitByName);
    }

    private Topic(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    private Topic(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    }

    public static List<LogWeEventEventResponse> getLogWeEventEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("LogWeEvent",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Utf8String>() {
                }));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<LogWeEventEventResponse> responses = new ArrayList<LogWeEventEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            LogWeEventEventResponse typedResponse = new LogWeEventEventResponse();
            typedResponse.topicName = (Bytes32) eventValues.getNonIndexedValues().get(0);
            typedResponse.eventSeq = (Uint256) eventValues.getNonIndexedValues().get(1);
            typedResponse.eventBlockNumer = (Uint256) eventValues.getNonIndexedValues().get(2);
            typedResponse.eventContent = (Utf8String) eventValues.getNonIndexedValues().get(3);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<LogWeEventEventResponse> logWeEventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("LogWeEvent",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Utf8String>() {
                }));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, LogWeEventEventResponse>() {
            @Override
            public LogWeEventEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                LogWeEventEventResponse typedResponse = new LogWeEventEventResponse();
                typedResponse.topicName = (Bytes32) eventValues.getNonIndexedValues().get(0);
                typedResponse.eventSeq = (Uint256) eventValues.getNonIndexedValues().get(1);
                typedResponse.eventBlockNumer = (Uint256) eventValues.getNonIndexedValues().get(2);
                typedResponse.eventContent = (Utf8String) eventValues.getNonIndexedValues().get(3);
                return typedResponse;
            }
        });
    }

    public Future<TransactionReceipt> publishWeEvent(Bytes32 topicName, Utf8String eventContent) {
        Function function = new Function("publishWeEvent", Arrays.<Type>asList(topicName, eventContent), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void publishWeEvent(Bytes32 topicName, Utf8String eventContent, TransactionSucCallback callback) {
        Function function = new Function("publishWeEvent", Arrays.<Type>asList(topicName, eventContent), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public static Future<Topic> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(Topic.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<Topic> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(Topic.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Topic load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    public static Topic load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    }

    public static Topic loadByName(String contractName, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractName, web3j, credentials, gasPrice, gasLimit, true);
    }

    public static Topic loadByName(String contractName, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractName, web3j, transactionManager, gasPrice, gasLimit, true);
    }

    public static class LogWeEventEventResponse {
        public Bytes32 topicName;

        public Uint256 eventSeq;

        public Uint256 eventBlockNumer;

        public Utf8String eventContent;
    }
}
