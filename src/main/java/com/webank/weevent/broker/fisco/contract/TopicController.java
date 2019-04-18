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
import org.bcos.web3j.abi.FunctionEncoder;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.Event;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
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
public final class TopicController extends Contract {
    private static String BINARY = "6060604052341561000c57fe5b604051602080610869833981016040528080519060200190919050505b80600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b505b6107ed8061007c6000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806325bc59511461005c578063a10d4f2f146100ef578063ac2976f61461018d578063af89bc43146101f1575bfe5b341561006457fe5b610083600480803590602001909190803590602001909190505061024c565b60405180838152602001806020018281038252838181518152602001915080519060200190602002808383600083146100db575b8051825260208311156100db576020820191506020810190506020830392506100b7565b505050905001935050505060405180910390f35b34156100f757fe5b6101116004808035600019169060200190919050506103e1565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b341561019557fe5b6101af6004808035600019169060200190919050506104bd565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156101f957fe5b61023260048080356000191690602001909190803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061057d565b604051808215151515815260200191505060405180910390f35b6000610256610780565b6000610260610794565b610268610780565b600060008711158061027a5750606487115b1561028457600a96505b60009350600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166338f1804489896000604051610cc001526040518363ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018083815260200182815260200192505050610cc060405180830381600087803b151561032857fe5b6102c65a03f1151561033657fe5b50505060405180519060200180519060200180610c80016040528095508196508298505050508360405180591061036a5750595b908082528060200260200182016040525b509150600090505b838110156103d257828160648110151561039957fe5b602002015182828151811015156103ac57fe5b9060200190602002019060001916908160001916815250505b8080600101915050610383565b8194505b505050509250929050565b600060006000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663cbea41d1856000604051606001526040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050606060405180830381600087803b151561048557fe5b6102c65a03f1151561049357fe5b505050604051805190602001805190602001805190508093508194508295505050505b9193909250565b6000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663ac2976f6836000604051602001526040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050602060405180830381600087803b151561055d57fe5b6102c65a03f1151561056b57fe5b5050506040518051905090505b919050565b6000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166308463c40846000604051602001526040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050602060405180830381600087803b151561061d57fe5b6102c65a03f1151561062b57fe5b505050604051805190501561067d577f13c45be6a13a69b72e3af68ac93a23b440f116e2fca46ff32202473842c771066207a1846040518082815260200191505060405180910390a16000905061077a565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16636d33e1c88484426000604051602001526040518463ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018084600019166000191681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019350505050602060405180830381600087803b151561075757fe5b6102c65a03f1151561076557fe5b50505060405180519050506001905061077a565b5b92915050565b602060405190810160405280600081525090565b610c80604051908101604052806064905b6000600019168152602001906001900390816107a557905050905600a165627a7a723058200da5bce31d605d6eac9ed813a0c43d9e86c266d557880c05d4f53ad3a22bd39f0029";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopicName\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"topicList\",\"type\":\"bytes32[]\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"}],\"name\":\"getTopicInfo\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"},{\"name\":\"senderAddress\",\"type\":\"address\"},{\"name\":\"createdTimestamp\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"}],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"},{\"name\":\"topicAddress\",\"type\":\"address\"}],\"name\":\"addTopicInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"topicDataAddress\",\"type\":\"address\"}],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"retCode\",\"type\":\"uint256\"}],\"name\":\"LogAddTopicNameAddress\",\"type\":\"event\"}]";

    private TopicController(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit, isInitByName);
    }

    private TopicController(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit, isInitByName);
    }

    private TopicController(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    private TopicController(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    }

    public static List<LogAddTopicNameAddressEventResponse> getLogAddTopicNameAddressEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("LogAddTopicNameAddress",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<LogAddTopicNameAddressEventResponse> responses = new ArrayList<LogAddTopicNameAddressEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            LogAddTopicNameAddressEventResponse typedResponse = new LogAddTopicNameAddressEventResponse();
            typedResponse.retCode = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<LogAddTopicNameAddressEventResponse> logAddTopicNameAddressEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("LogAddTopicNameAddress",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, LogAddTopicNameAddressEventResponse>() {
            @Override
            public LogAddTopicNameAddressEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                LogAddTopicNameAddressEventResponse typedResponse = new LogAddTopicNameAddressEventResponse();
                typedResponse.retCode = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Future<List<Type>> listTopicName(Uint256 pageIndex, Uint256 pageSize) {
        Function function = new Function("listTopicName",
                Arrays.<Type>asList(pageIndex, pageSize),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<DynamicArray<Bytes32>>() {
                }));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<List<Type>> getTopicInfo(Bytes32 topicName) {
        Function function = new Function("getTopicInfo",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }, new TypeReference<Address>() {
                }, new TypeReference<Uint256>() {
                }));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<Address> getTopicAddress(Bytes32 topicName) {
        Function function = new Function("getTopicAddress",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> addTopicInfo(Bytes32 topicName, Address topicAddress) {
        Function function = new Function("addTopicInfo", Arrays.<Type>asList(topicName, topicAddress), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void addTopicInfo(Bytes32 topicName, Address topicAddress, TransactionSucCallback callback) {
        Function function = new Function("addTopicInfo", Arrays.<Type>asList(topicName, topicAddress), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public static Future<TopicController> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Address topicDataAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(topicDataAddress));
        return deployAsync(TopicController.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Future<TopicController> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Address topicDataAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(topicDataAddress));
        return deployAsync(TopicController.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static TopicController load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController(contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    public static TopicController load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController(contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    }

    public static TopicController loadByName(String contractName, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController(contractName, web3j, credentials, gasPrice, gasLimit, true);
    }

    public static TopicController loadByName(String contractName, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController(contractName, web3j, transactionManager, gasPrice, gasLimit, true);
    }

    public static class LogAddTopicNameAddressEventResponse {
        public Uint256 retCode;
    }
}
