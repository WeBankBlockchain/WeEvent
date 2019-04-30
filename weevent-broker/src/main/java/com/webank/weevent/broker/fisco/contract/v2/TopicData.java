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
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
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
public class TopicData extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b506108b5806100206000396000f300608060405260043610610078576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308463c401461007d57806324e615c5146100c657806338f180441461010f5780636d33e1c814610190578063ac2976f614610203578063cbea41d114610274575b600080fd5b34801561008957600080fd5b506100ac600480360381019080803560001916906020019092919050505061031f565b604051808215151515815260200191505060405180910390f35b3480156100d257600080fd5b506100f160048036038101908080359060200190929190505050610418565b60405180826000191660001916815260200191505060405180910390f35b34801561011b57600080fd5b50610144600480360381019080803590602001909291908035906020019092919050505061043b565b6040518084815260200183815260200182606460200280838360005b8381101561017b578082015181840152602081019050610160565b50505050905001935050505060405180910390f35b34801561019c57600080fd5b506101e96004803603810190808035600019169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506104d2565b604051808215151515815260200191505060405180910390f35b34801561020f57600080fd5b50610232600480360381019080803560001916906020019092919050505061061a565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34801561028057600080fd5b506102a3600480360381019080803560001916906020019092919050505061070f565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b6000610329610817565b6000808460001916600019168152602001908152602001600020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806040015160001415915050919050565b60018181548110151561042757fe5b906000526020600020016000915090505481565b600080610446610865565b6000808587029150600090505b858110156104be576001805490508210151561046e576104be565b60018281548110151561047d57fe5b9060005260206000200154838260648110151561049657fe5b6020020190600019169081600019168152505081806001019250508080600101915050610453565b600180549050945080935050509250925092565b60006104dc610817565b6060604051908101604052808573ffffffffffffffffffffffffffffffffffffffff1681526020013273ffffffffffffffffffffffffffffffffffffffff16815260200184815250905080600080876000191660001916815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020155905050600185908060018154018082558091505090600182039060005260206000200160009091929091909150906000191690555060019150509392505050565b6000610624610817565b6000808460001916600019168152602001908152602001600020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160028201548152505090508060000151915050919050565b600080600061071c610817565b6000808660001916600019168152602001908152602001600020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806000015193508060200151925080604001519150509193909250565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff168152602001600081525090565b610c80604051908101604052806064906020820280388339808201915050905050905600a165627a7a7230582032db77178f3e4c9e1eea2d8d59e51f1d2cb9dfbdc1096d71e7cd2558aafe720d0029";

    public static final String FUNC_ISTOPICEXIST = "isTopicExist";

    public static final String FUNC__TOPICARRAY = "_topicArray";

    public static final String FUNC_LISTTOPIC = "listTopic";

    public static final String FUNC_PUTTOPIC = "putTopic";

    public static final String FUNC_GETTOPICADDRESS = "getTopicAddress";

    public static final String FUNC_GETTOPIC = "getTopic";

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

    public RemoteCall<Boolean> isTopicExist(byte[] topicName) {
        final Function function = new Function(FUNC_ISTOPICEXIST,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<byte[]> _topicArray(BigInteger param0) {
        final Function function = new Function(FUNC__TOPICARRAY,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<Tuple3<BigInteger, BigInteger, List<byte[]>>> listTopic(BigInteger pageIndex, BigInteger pageSize) {
        final Function function = new Function(FUNC_LISTTOPIC,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageIndex),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageSize)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<StaticArray<Bytes32>>() {
                }));
        return new RemoteCall<Tuple3<BigInteger, BigInteger, List<byte[]>>>(
                new Callable<Tuple3<BigInteger, BigInteger, List<byte[]>>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, List<byte[]>>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                convertToNative((List<Bytes32>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteCall<TransactionReceipt> putTopic(byte[] topicName, String topicAddress, BigInteger createdTimestamp) {
        final Function function = new Function(
                FUNC_PUTTOPIC,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(createdTimestamp)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void putTopic(byte[] topicName, String topicAddress, BigInteger createdTimestamp, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_PUTTOPIC,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(createdTimestamp)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public RemoteCall<String> getTopicAddress(byte[] topicName) {
        final Function function = new Function(FUNC_GETTOPICADDRESS,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<Tuple3<String, String, BigInteger>> getTopic(byte[] topicName) {
        final Function function = new Function(FUNC_GETTOPIC,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName)),
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
