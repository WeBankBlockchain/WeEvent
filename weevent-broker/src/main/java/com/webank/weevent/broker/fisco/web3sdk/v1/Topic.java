package com.webank.weevent.broker.fisco.web3sdk.v1;

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
 *
 * <p>Generated with web3j version none.
 */
public final class Topic extends Contract {
    private static String BINARY = "606060405260006000556000600155341561001657fe5b5b6103aa806100266000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806342af35fd1461005157806342cbb15c14610077578063a99077f21461009d575bfe5b341561005957fe5b610061610195565b6040518082815260200191505060405180910390f35b341561007f57fe5b6100876101a0565b6040518082815260200191505060405180910390f35b34156100a557fe5b61017b600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506101ab565b604051808215151515815260200191505060405180910390f35b600060005490505b90565b600060015490505b90565b6000436001819055506001600054016000819055507f1c0e3e64fa3c39ea839f697c66d1bc2b3d10be51172d570c18012b860e5a5832846000544386866040518080602001868152602001858152602001806020018060200184810384528981815181526020019150805190602001908083836000831461024b575b80518252602083111561024b57602082019150602081019050602083039250610227565b505050905090810190601f1680156102775780820380516001836020036101000a031916815260200191505b508481038352868181518152602001915080519060200190808383600083146102bf575b8051825260208311156102bf5760208201915060208101905060208303925061029b565b505050905090810190601f1680156102eb5780820380516001836020036101000a031916815260200191505b50848103825285818151815260200191508051906020019080838360008314610333575b8051825260208311156103335760208201915060208101905060208303925061030f565b505050905090810190601f16801561035f5780820380516001836020036101000a031916815260200191505b509850505050505050505060405180910390a1600190505b93925050505600a165627a7a72305820cca355e278a302734ab16dec6cbc6747e569c310679caf154dcfc217bb70ab900029";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[],\"name\":\"getSequenceNumber\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getBlockNumber\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"eventContent\",\"type\":\"string\"},{\"name\":\"extensions\",\"type\":\"string\"}],\"name\":\"publishWeEvent\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"topicName\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"eventSeq\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"eventBlockNumer\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"eventContent\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"extensions\",\"type\":\"string\"}],\"name\":\"LogWeEvent\",\"type\":\"event\"}]";

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
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Utf8String>() {
                }, new TypeReference<Utf8String>() {
                }));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<LogWeEventEventResponse> responses = new ArrayList<LogWeEventEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            LogWeEventEventResponse typedResponse = new LogWeEventEventResponse();
            typedResponse.topicName = (Utf8String) eventValues.getNonIndexedValues().get(0);
            typedResponse.eventSeq = (Uint256) eventValues.getNonIndexedValues().get(1);
            typedResponse.eventBlockNumer = (Uint256) eventValues.getNonIndexedValues().get(2);
            typedResponse.eventContent = (Utf8String) eventValues.getNonIndexedValues().get(3);
            typedResponse.extensions = (Utf8String) eventValues.getNonIndexedValues().get(4);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<LogWeEventEventResponse> logWeEventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("LogWeEvent",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Utf8String>() {
                }, new TypeReference<Utf8String>() {
                }));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, LogWeEventEventResponse>() {
            @Override
            public LogWeEventEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                LogWeEventEventResponse typedResponse = new LogWeEventEventResponse();
                typedResponse.topicName = (Utf8String) eventValues.getNonIndexedValues().get(0);
                typedResponse.eventSeq = (Uint256) eventValues.getNonIndexedValues().get(1);
                typedResponse.eventBlockNumer = (Uint256) eventValues.getNonIndexedValues().get(2);
                typedResponse.eventContent = (Utf8String) eventValues.getNonIndexedValues().get(3);
                typedResponse.extensions = (Utf8String) eventValues.getNonIndexedValues().get(4);
                return typedResponse;
            }
        });
    }

    public Future<Uint256> getSequenceNumber() {
        Function function = new Function("getSequenceNumber",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> getBlockNumber() {
        Function function = new Function("getBlockNumber",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> publishWeEvent(Utf8String topicName, Utf8String eventContent, Utf8String extensions) {
        Function function = new Function("publishWeEvent", Arrays.<Type>asList(topicName, eventContent, extensions), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void publishWeEvent(Utf8String topicName, Utf8String eventContent, Utf8String extensions, TransactionSucCallback callback) {
        Function function = new Function("publishWeEvent", Arrays.<Type>asList(topicName, eventContent, extensions), Collections.<TypeReference<?>>emptyList());
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
        public Utf8String topicName;

        public Uint256 eventSeq;

        public Uint256 eventBlockNumer;

        public Utf8String eventContent;

        public Utf8String extensions;
    }
}
