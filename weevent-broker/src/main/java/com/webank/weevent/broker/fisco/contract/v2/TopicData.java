package com.webank.weevent.broker.fisco.contract.v2;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bool;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.StaticArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.TransactionManager;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version none.
 */
public class TopicData extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b506111f3806100206000396000f30060806040526004361061008e576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806309eb60f31461009357806338f1804414610139578063474502d8146101e9578063563eeb00146102cc5780636d7dbe8514610377578063ae501376146103f8578063bfe370d9146104a1578063e503bab614610526575b600080fd5b34801561009f57600080fd5b506100be600480360381019080803590602001909291905050506105ab565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100fe5780820151818401526020810190506100e3565b50505050905090810190601f16801561012b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561014557600080fd5b5061016e6004803603810190808035906020019092919080359060200190929190505050610666565b6040518085815260200184815260200183606460200280838360005b838110156101a557808201518184015260208101905061018a565b5050505090500182606460200280838360005b838110156101d35780820151818401526020810190506101b8565b5050505090500194505050505060405180910390f35b3480156101f557600080fd5b50610250600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610b05565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b3480156102d857600080fd5b5061035d600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610c5e565b604051808215151515815260200191505060405180910390f35b34801561038357600080fd5b506103de600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610e04565b604051808215151515815260200191505060405180910390f35b34801561040457600080fd5b5061045f600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610f4e565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156104ad57600080fd5b50610508600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050611094565b60405180826000191660001916815260200191505060405180910390f35b34801561053257600080fd5b5061058d600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192905050506110a2565b60405180826000191660001916815260200191505060405180910390f35b6001818154811015156105ba57fe5b906000526020600020016000915090508054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561065e5780601f106106335761010080835404028352916020019161065e565b820191906000526020600020905b81548152906001019060200180831161064157829003601f168201915b505050505081565b6000806106716110b0565b6106796110b0565b60008060608060608060606000808d8f029850600097505b8d881015610ae857600180549050891015156106ac57610ae8565b602060018a8154811015156106bd57fe5b906000526020600020018054600181600116156101000203166002900490501115156107e65761079d60018a8154811015156106f557fe5b906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107935780601f1061076857610100808354040283529160200191610793565b820191906000526020600020905b81548152906001019060200180831161077657829003601f168201915b50505050506110a2565b8b896064811015156107ab57fe5b6020020190600019169081600019168152505060008a896064811015156107ce57fe5b60200201906000191690816000191681525050610ad3565b6001898154811015156107f557fe5b906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108935780601f1061086857610100808354040283529160200191610893565b820191906000526020600020905b81548152906001019060200180831161087657829003601f168201915b5050505050965060206040519080825280601f01601f1916602001820160405280156108ce5781602001602082028038833980820191505090505b50955060206040519080825280601f01601f1916602001820160405280156109055781602001602082028038833980820191505090505b509450859350849250600091505b60208210156109c057868281518110151561092a57fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f010000000000000000000000000000000000000000000000000000000000000002848381518110151561098357fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053508180600101925050610913565b60009050602091505b8651821015610a7e5786828151811015156109e057fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f0100000000000000000000000000000000000000000000000000000000000000028382815181101515610a3957fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a905350808060010191505081806001019250506109c9565b610a8784611094565b8b89606481101515610a9557fe5b60200201906000191690816000191681525050610ab183611094565b8a89606481101515610abf57fe5b602002019060001916908160001916815250505b88806001019950508780600101985050610691565b6001805490509c50879b5050505050505050505092959194509250565b6000806000610b126110d4565b6000856040518082805190602001908083835b602083101515610b4a5780518252602082019150602081019050602083039250610b25565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806000015193508060200151925080604001519150509193909250565b6000610c686110d4565b6060604051908101604052808573ffffffffffffffffffffffffffffffffffffffff1681526020013273ffffffffffffffffffffffffffffffffffffffff168152602001848152509050806000866040518082805190602001908083835b602083101515610ceb5780518252602082019150602081019050602083039250610cc6565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550604082015181600201559050506001859080600181540180825580915050906001820390600052602060002001600090919290919091509080519060200190610df6929190611122565b505060019150509392505050565b6000610e0e6110d4565b6000836040518082805190602001908083835b602083101515610e465780518252602082019150602081019050602083039250610e21565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806040015160001415915050919050565b6000610f586110d4565b6000836040518082805190602001908083835b602083101515610f905780518252602082019150602081019050602083039250610f6b565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160028201548152505090508060000151915050919050565b600060208201519050919050565b600060208201519050919050565b610c8060405190810160405280606490602082028038833980820191505090505090565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff168152602001600081525090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061116357805160ff1916838001178555611191565b82800160010185558215611191579182015b82811115611190578251825591602001919060010190611175565b5b50905061119e91906111a2565b5090565b6111c491905b808211156111c05760008160009055506001016111a8565b5090565b905600a165627a7a723058202108a74003098a340ef583f6ac5cec23e97db5b0d9570542092f9325d7637f8a0029";

    public static final String FUNC__TOPICSTRINGARRAY = "_topicStringArray";

    public static final String FUNC_LISTTOPIC = "listTopic";

    public static final String FUNC_GETTOPIC = "getTopic";

    public static final String FUNC_PUTTOPIC = "putTopic";

    public static final String FUNC_ISTOPICEXIST = "isTopicExist";

    public static final String FUNC_GETTOPICADDRESS = "getTopicAddress";

    public static final String FUNC_BYTESTOBYTES32 = "bytesToBytes32";

    public static final String FUNC_STRINGTOBYTESVER2 = "stringToBytesVer2";

    @Deprecated
    protected TopicData(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TopicData(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TopicData(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TopicData(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<String> _topicStringArray(BigInteger param0) {
        final Function function = new Function(FUNC__TOPICSTRINGARRAY,
                Arrays.<Type>asList(new Uint256(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<Tuple4<BigInteger, BigInteger, List<byte[]>, List<byte[]>>> listTopic(BigInteger pageIndex, BigInteger pageSize) {
        final Function function = new Function(FUNC_LISTTOPIC,
                Arrays.<Type>asList(new Uint256(pageIndex),
                        new Uint256(pageSize)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<StaticArray<Bytes32>>() {
                }, new TypeReference<StaticArray<Bytes32>>() {
                }));
        return new RemoteCall<Tuple4<BigInteger, BigInteger, List<byte[]>, List<byte[]>>>(
                new Callable<Tuple4<BigInteger, BigInteger, List<byte[]>, List<byte[]>>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, List<byte[]>, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, List<byte[]>, List<byte[]>>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                convertToNative((List<Bytes32>) results.get(2).getValue()),
                                convertToNative((List<Bytes32>) results.get(3).getValue()));
                    }
                });
    }

    public RemoteCall<Tuple3<String, String, BigInteger>> getTopic(String topicName) {
        final Function function = new Function(FUNC_GETTOPIC,
                Arrays.<Type>asList(new Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }, new TypeReference<Address>() {
                }, new TypeReference<Uint256>() {
                }));
        return new RemoteCall<Tuple3<String, String, BigInteger>>(
                new Callable<Tuple3<String, String, BigInteger>>() {
                    @Override
                    public Tuple3<String, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, BigInteger>(
                                (String) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> putTopic(String topicName, String topicAddress, BigInteger createdTimestamp) {
        final Function function = new Function(
                FUNC_PUTTOPIC,
                Arrays.<Type>asList(new Utf8String(topicName),
                        new Address(topicAddress),
                        new Uint256(createdTimestamp)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void putTopic(String topicName, String topicAddress, BigInteger createdTimestamp, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_PUTTOPIC,
                Arrays.<Type>asList(new Utf8String(topicName),
                        new Address(topicAddress),
                        new Uint256(createdTimestamp)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public RemoteCall<Boolean> isTopicExist(String topicName) {
        final Function function = new Function(FUNC_ISTOPICEXIST,
                Arrays.<Type>asList(new Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> getTopicAddress(String topicName) {
        final Function function = new Function(FUNC_GETTOPICADDRESS,
                Arrays.<Type>asList(new Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> bytesToBytes32(byte[] source) {
        final Function function = new Function(
                FUNC_BYTESTOBYTES32,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(source)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void bytesToBytes32(byte[] source, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_BYTESTOBYTES32,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.DynamicBytes(source)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public RemoteCall<TransactionReceipt> stringToBytesVer2(String source) {
        final Function function = new Function(
                FUNC_STRINGTOBYTESVER2,
                Arrays.<Type>asList(new Utf8String(source)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void stringToBytesVer2(String source, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_STRINGTOBYTESVER2,
                Arrays.<Type>asList(new Utf8String(source)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    @Deprecated
    public static TopicData load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicData(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TopicData load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicData(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TopicData load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new TopicData(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TopicData load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TopicData(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TopicData> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TopicData.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TopicData> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TopicData.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<TopicData> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TopicData.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TopicData> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TopicData.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
