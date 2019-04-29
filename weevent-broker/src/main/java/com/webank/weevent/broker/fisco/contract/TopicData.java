package com.webank.weevent.broker.fisco.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.StaticArray;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;
import org.bcos.web3j.tx.TransactionManager;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.bcos.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 * <p>Generated with web3j version none.
 */
public final class TopicData extends Contract {
    private static String BINARY = "6060604052341561000c57fe5b5b6108d98061001c6000396000f30060606040523615610076576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308463c401461007857806324e615c5146100b457806338f18044146100f05780636d33e1c814610171578063ac2976f6146101d5578063cbea41d114610239575bfe5b341561008057fe5b61009a6004808035600019169060200190919050506102d7565b604051808215151515815260200191505060405180910390f35b34156100bc57fe5b6100d260048080359060200190919050506103d2565b60405180826000191660001916815260200191505060405180910390f35b34156100f857fe5b61011760048080359060200190919080359060200190919050506103f7565b604051808481526020018381526020018260646020028083836000831461015d575b80518252602083111561015d57602082019150602081019050602083039250610139565b505050905001935050505060405180910390f35b341561017957fe5b6101bb60048080356000191690602001909190803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610497565b604051808215151515815260200191505060405180910390f35b34156101dd57fe5b6101f76004808035600019169060200190919050506105df565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561024157fe5b61025b6004808035600019169060200190919050506106d6565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b60006102e16107e1565b600060008460001916600019168152602001908152602001600020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600282015481525050905080604001516000141591505b50919050565b6001818154811015156103e157fe5b906000526020600020900160005b915090505481565b6000600061040361082f565b600060008587029150600090505b85811015610482576001805490508210151561042c57610482565b60018281548110151561043b57fe5b906000526020600020900160005b5054838260648110151561045957fe5b6020020190600019169081600019168152505081806001019250505b8080600101915050610411565b60018054905094508093505b50509250925092565b60006104a16107e1565b6060604051908101604052808573ffffffffffffffffffffffffffffffffffffffff1681526020013273ffffffffffffffffffffffffffffffffffffffff1681526020018481525090508060006000876000191660001916815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020155905050600180548060010182816105b6919061085c565b916000526020600020900160005b8790919091509060001916905550600191505b509392505050565b60006105e96107e1565b600060008460001916600019168152602001908152602001600020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806000015191505b50919050565b6000600060006106e46107e1565b600060008660001916600019168152602001908152602001600020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160028201548152505090508060000151935080602001519250806040015191505b509193909250565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff168152602001600081525090565b610c80604051908101604052806064905b6000600019168152602001906001900390816108405790505090565b815481835581811511610883578183600052602060002091820191016108829190610888565b5b505050565b6108aa91905b808211156108a657600081600090555060010161088e565b5090565b905600a165627a7a723058206c52af3b6e4417d753cb1de612021ede66d7b882ecba07bd79df86c5fe1b115c0029";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"}],\"name\":\"isTopicExist\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"_topicArray\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopic\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"size\",\"type\":\"uint256\"},{\"name\":\"topicList\",\"type\":\"bytes32[100]\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"},{\"name\":\"topicAddress\",\"type\":\"address\"},{\"name\":\"createdTimestamp\",\"type\":\"uint256\"}],\"name\":\"putTopic\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"}],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"bytes32\"}],\"name\":\"getTopic\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"},{\"name\":\"senderAddress\",\"type\":\"address\"},{\"name\":\"createTimestamp\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"}]";

    private TopicData(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit, isInitByName);
    }

    private TopicData(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Boolean isInitByName) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit, isInitByName);
    }

    private TopicData(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    private TopicData(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    }

    public Future<Bool> isTopicExist(Bytes32 topicName) {
        Function function = new Function("isTopicExist",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Bytes32> _topicArray(Uint256 param0) {
        Function function = new Function("_topicArray",
                Arrays.<Type>asList(param0),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<List<Type>> listTopic(Uint256 pageIndex, Uint256 pageSize) {
        Function function = new Function("listTopic",
                Arrays.<Type>asList(pageIndex, pageSize),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<StaticArray<Bytes32>>() {
                }));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> putTopic(Bytes32 topicName, Address topicAddress, Uint256 createdTimestamp) {
        Function function = new Function("putTopic", Arrays.<Type>asList(topicName, topicAddress, createdTimestamp), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void putTopic(Bytes32 topicName, Address topicAddress, Uint256 createdTimestamp, TransactionSucCallback callback) {
        Function function = new Function("putTopic", Arrays.<Type>asList(topicName, topicAddress, createdTimestamp), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Address> getTopicAddress(Bytes32 topicName) {
        Function function = new Function("getTopicAddress",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<List<Type>> getTopic(Bytes32 topicName) {
        Function function = new Function("getTopic",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }, new TypeReference<Address>() {
                }, new TypeReference<Uint256>() {
                }));
        return executeCallMultipleValueReturnAsync(function);
    }

    public static Future<TopicData> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(TopicData.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<TopicData> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(TopicData.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static TopicData load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicData(contractAddress, web3j, credentials, gasPrice, gasLimit, false);
    }

    public static TopicData load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicData(contractAddress, web3j, transactionManager, gasPrice, gasLimit, false);
    }

    public static TopicData loadByName(String contractName, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicData(contractName, web3j, credentials, gasPrice, gasLimit, true);
    }

    public static TopicData loadByName(String contractName, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicData(contractName, web3j, transactionManager, gasPrice, gasLimit, true);
    }
}
