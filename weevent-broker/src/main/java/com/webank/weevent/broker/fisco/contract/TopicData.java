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
import org.bcos.web3j.abi.datatypes.DynamicBytes;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.StaticArray;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
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
    private static String BINARY = "6060604052341561000c57fe5b5b61127f8061001c6000396000f3006060604052361561008c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806309eb60f31461008e57806338f1804414610135578063474502d8146101f3578063563eeb00146102c75780636d7dbe8514610361578063ae501376146103d3578063bfe370d91461046d578063e503bab6146104e3575bfe5b341561009657fe5b6100ac6004808035906020019091905050610559565b60405180806020018281038252838181518152602001915080519060200190808383600083146100fb575b8051825260208311156100fb576020820191506020810190506020830392506100d7565b505050905090810190601f1680156101275780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561013d57fe5b61015c6004808035906020019091908035906020019091905050610616565b60405180858152602001848152602001836064602002808383600083146101a2575b8051825260208311156101a25760208201915060208101905060208303925061017e565b505050905001826064602002808383600083146101de575b8051825260208311156101de576020820191506020810190506020830392506101ba565b50505090500194505050505060405180910390f35b34156101fb57fe5b61024b600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610ac3565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b34156102cf57fe5b610347600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610c1c565b604051808215151515815260200191505060405180910390f35b341561036957fe5b6103b9600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610dbf565b604051808215151515815260200191505060405180910390f35b34156103db57fe5b61042b600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610f08565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561047557fe5b6104c5600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190505061104d565b60405180826000191660001916815260200191505060405180910390f35b34156104eb57fe5b61053b600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190505061105c565b60405180826000191660001916815260200191505060405180910390f35b60018181548110151561056857fe5b906000526020600020900160005b915090508054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561060e5780601f106105e35761010080835404028352916020019161060e565b820191906000526020600020905b8154815290600101906020018083116105f157829003601f168201915b505050505081565b6000600061062261106b565b61062a61106b565b60006000610636611098565b61063e6110ac565b6106466110ac565b61064e611098565b610656611098565b600060008d8f029850600097505b8d881015610aa5576001805490508910151561067f57610aa5565b602060018a81548110151561069057fe5b906000526020600020900160005b508054600181600116156101000203166002900490501115156107c35761077a60018a8154811015156106cd57fe5b906000526020600020900160005b508054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107705780601f1061074557610100808354040283529160200191610770565b820191906000526020600020905b81548152906001019060200180831161075357829003601f168201915b505050505061105c565b8b8960648110151561078857fe5b6020020190600019169081600019168152505060008a896064811015156107ab57fe5b60200201906000191690816000191681525050610a8f565b6001898154811015156107d257fe5b906000526020600020900160005b508054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108755780601f1061084a57610100808354040283529160200191610875565b820191906000526020600020905b81548152906001019060200180831161085857829003601f168201915b50505050509650602060405180591061088b5750595b908082528060200260200182016040525b50955060206040518059106108ae5750595b908082528060200260200182016040525b509450859350849250600091505b602082101561097b5786828151811015156108e457fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f010000000000000000000000000000000000000000000000000000000000000002848381518110151561093d57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b81806001019250506108cd565b60009050602091505b8651821015610a3a57868281518110151561099b57fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f01000000000000000000000000000000000000000000000000000000000000000283828151811015156109f457fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535080806001019150505b8180600101925050610984565b610a438461104d565b8b89606481101515610a5157fe5b60200201906000191690816000191681525050610a6d8361104d565b8a89606481101515610a7b57fe5b602002019060001916908160001916815250505b88806001019950505b8780600101985050610664565b6001805490509c50879b505b50505050505050505092959194509250565b600060006000610ad16110c0565b6000856040518082805190602001908083835b60208310610b075780518252602082019150602081019050602083039250610ae4565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160028201548152505090508060000151935080602001519250806040015191505b509193909250565b6000610c266110c0565b6060604051908101604052808573ffffffffffffffffffffffffffffffffffffffff1681526020013273ffffffffffffffffffffffffffffffffffffffff168152602001848152509050806000866040518082805190602001908083835b60208310610ca75780518252602082019150602081019050602083039250610c84565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506040820151816002015590505060018054806001018281610d89919061110e565b916000526020600020900160005b8790919091509080519060200190610db092919061113a565b5050600191505b509392505050565b6000610dc96110c0565b6000836040518082805190602001908083835b60208310610dff5780518252602082019150602081019050602083039250610ddc565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600282015481525050905080604001516000141591505b50919050565b6000610f126110c0565b6000836040518082805190602001908083835b60208310610f485780518252602082019150602081019050602083039250610f25565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806000015191505b50919050565b6000602082015190505b919050565b6000602082015190505b919050565b610c80604051908101604052806064905b60006000191681526020019060019003908161107c5790505090565b602060405190810160405280600081525090565b602060405190810160405280600081525090565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff168152602001600081525090565b8154818355818115116111355781836000526020600020918201910161113491906111ba565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061117b57805160ff19168380011785556111a9565b828001600101855582156111a9579182015b828111156111a857825182559160200191906001019061118d565b5b5090506111b691906111e6565b5090565b6111e391905b808211156111df57600081816111d6919061120b565b506001016111c0565b5090565b90565b61120891905b808211156112045760008160009055506001016111ec565b5090565b90565b50805460018160011615610100020316600290046000825580601f106112315750611250565b601f01602090049060005260206000209081019061124f91906111e6565b5b505600a165627a7a723058203456e75e6782c77925c609b5ea09188fb04d68177c1bee72eccecb7d329f01f50029";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"_topicStringArray\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopic\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"size\",\"type\":\"uint256\"},{\"name\":\"topicList1\",\"type\":\"bytes32[100]\"},{\"name\":\"topicList2\",\"type\":\"bytes32[100]\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopic\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"},{\"name\":\"senderAddress\",\"type\":\"address\"},{\"name\":\"createTimestamp\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"topicAddress\",\"type\":\"address\"},{\"name\":\"createdTimestamp\",\"type\":\"uint256\"}],\"name\":\"putTopic\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"isTopicExist\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"source\",\"type\":\"bytes\"}],\"name\":\"bytesToBytes32\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"source\",\"type\":\"string\"}],\"name\":\"stringToBytesVer2\",\"outputs\":[{\"name\":\"result\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"}]";

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

    public Future<Utf8String> _topicStringArray(Uint256 param0) {
        Function function = new Function("_topicStringArray",
                Arrays.<Type>asList(param0),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<List<Type>> listTopic(Uint256 pageIndex, Uint256 pageSize) {
        Function function = new Function("listTopic",
                Arrays.<Type>asList(pageIndex, pageSize),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<StaticArray<Bytes32>>() {
                }, new TypeReference<StaticArray<Bytes32>>() {
                }));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<List<Type>> getTopic(Utf8String topicName) {
        Function function = new Function("getTopic",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }, new TypeReference<Address>() {
                }, new TypeReference<Uint256>() {
                }));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> putTopic(Utf8String topicName, Address topicAddress, Uint256 createdTimestamp) {
        Function function = new Function("putTopic", Arrays.<Type>asList(topicName, topicAddress, createdTimestamp), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void putTopic(Utf8String topicName, Address topicAddress, Uint256 createdTimestamp, TransactionSucCallback callback) {
        Function function = new Function("putTopic", Arrays.<Type>asList(topicName, topicAddress, createdTimestamp), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<Bool> isTopicExist(Utf8String topicName) {
        Function function = new Function("isTopicExist",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Address> getTopicAddress(Utf8String topicName) {
        Function function = new Function("getTopicAddress",
                Arrays.<Type>asList(topicName),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> bytesToBytes32(DynamicBytes source) {
        Function function = new Function("bytesToBytes32", Arrays.<Type>asList(source), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void bytesToBytes32(DynamicBytes source, TransactionSucCallback callback) {
        Function function = new Function("bytesToBytes32", Arrays.<Type>asList(source), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<TransactionReceipt> stringToBytesVer2(Utf8String source) {
        Function function = new Function("stringToBytesVer2", Arrays.<Type>asList(source), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void stringToBytesVer2(Utf8String source, TransactionSucCallback callback) {
        Function function = new Function("stringToBytesVer2", Arrays.<Type>asList(source), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
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
