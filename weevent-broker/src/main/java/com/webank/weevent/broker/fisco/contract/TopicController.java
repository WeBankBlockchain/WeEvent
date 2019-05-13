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
 *
 * <p>Generated with web3j version none.
 */
public final class TopicController extends Contract {
    private static String BINARY = "6060604052341561000c57fe5b604051602080610b8e833981016040528080519060200190919050505b80600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b505b610b128061007c6000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806325bc59511461005c578063912849fe14610145578063ae50137614610219578063fc3a2330146102b3575bfe5b341561006457fe5b6100836004808035906020019091908035906020019091905050610344565b6040518084815260200180602001806020018381038352858181518152602001915080519060200190602002808383600083146100df575b8051825260208311156100df576020820191506020810190506020830392506100bb565b50505090500183810382528481815181526020019150805190602001906020028083836000831461012f575b80518252602083111561012f5760208201915060208101905060208303925061010b565b5050509050019550505050505060405180910390f35b341561014d57fe5b61019d600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190505061055a565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b341561022157fe5b610271600480803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506106a1565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156102bb57fe5b61032a600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff169060200190919050506107cc565b604051808215151515815260200191505060405180910390f35b600061034e610aa5565b610356610aa5565b6000610360610ab9565b610368610ab9565b610370610aa5565b610378610aa5565b600060008a11158061038a575060648a115b1561039457600a99505b60009550600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166338f180448c8c600060405161194001526040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808381526020018281526020019250505061194060405180830381600087803b151561043857fe5b6102c65a03f1151561044657fe5b50505060405180519060200180519060200180610c800180610c8001604052809750819850829950839c5050505050856040518059106104835750595b908082528060200260200182016040525b509250856040518059106104a55750595b908082528060200260200182016040525b509150600090505b858110156105465784816064811015156104d457fe5b602002015183828151811015156104e757fe5b906020019060200201906000191690816000191681525050838160648110151561050d57fe5b6020020151828281518110151561052057fe5b9060200190602002019060001916908160001916815250505b80806001019150506104be565b8297508196505b5050505050509250925092565b600060006000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663474502d8856000604051606001526040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825283818151815260200191508051906020019080838360008314610622575b805182526020831115610622576020820191506020810190506020830392506105fe565b505050905090810190601f16801561064e5780820380516001836020036101000a031916815260200191505b5092505050606060405180830381600087803b151561066957fe5b6102c65a03f1151561067757fe5b505050604051805190602001805190602001805190508093508194508295505050505b9193909250565b6000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663ae501376836000604051602001526040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825283818151815260200191508051906020019080838360008314610765575b80518252602083111561076557602082019150602081019050602083039250610741565b505050905090810190601f1680156107915780820380516001836020036101000a031916815260200191505b5092505050602060405180830381600087803b15156107ac57fe5b6102c65a03f115156107ba57fe5b5050506040518051905090505b919050565b6000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16636d7dbe85846000604051602001526040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825283818151815260200191508051906020019080838360008314610890575b8051825260208311156108905760208201915060208101905060208303925061086c565b505050905090810190601f1680156108bc5780820380516001836020036101000a031916815260200191505b5092505050602060405180830381600087803b15156108d757fe5b6102c65a03f115156108e557fe5b5050506040518051905015610937577f13c45be6a13a69b72e3af68ac93a23b440f116e2fca46ff32202473842c771066207a1846040518082815260200191505060405180910390a160009050610a9f565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663563eeb008484426000604051602001526040518463ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040180806020018473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001838152602001828103825285818151815260200191508051906020019080838360008314610a33575b805182526020831115610a3357602082019150602081019050602083039250610a0f565b505050905090810190601f168015610a5f5780820380516001836020036101000a031916815260200191505b50945050505050602060405180830381600087803b1515610a7c57fe5b6102c65a03f11515610a8a57fe5b505050604051805190505060019050610a9f565b5b92915050565b602060405190810160405280600081525090565b610c80604051908101604052806064905b600060001916815260200190600190039081610aca57905050905600a165627a7a72305820e2b5c04016100374d68bae668de6fcbbac260abf83291f336f2d28f3d01e189d0029";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopicName\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"topicList1\",\"type\":\"bytes32[]\"},{\"name\":\"topicList2\",\"type\":\"bytes32[]\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopicInfo\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"},{\"name\":\"senderAddress\",\"type\":\"address\"},{\"name\":\"createdTimestamp\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"topicAddress\",\"type\":\"address\"}],\"name\":\"addTopicInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"topicDataAddress\",\"type\":\"address\"}],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"retCode\",\"type\":\"uint256\"}],\"name\":\"LogAddTopicNameAddress\",\"type\":\"event\"}]";

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
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
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
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
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
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<List<Type>> getTopicInfo(Utf8String topicName) {
        Function function = new Function("getTopicInfo", 
                Arrays.<Type>asList(topicName), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<Address> getTopicAddress(Utf8String topicName) {
        Function function = new Function("getTopicAddress", 
                Arrays.<Type>asList(topicName), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> addTopicInfo(Utf8String topicName, Address topicAddress) {
        Function function = new Function("addTopicInfo", Arrays.<Type>asList(topicName, topicAddress), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void addTopicInfo(Utf8String topicName, Address topicAddress, TransactionSucCallback callback) {
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
