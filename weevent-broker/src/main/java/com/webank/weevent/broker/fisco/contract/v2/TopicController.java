package com.webank.weevent.broker.fisco.contract.v2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.EventEncoder;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Event;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.request.BcosFilter;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
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
public class TopicController extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060405160208061092283398101806040528101908080519060200190929190505050806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505061089f806100836000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806325bc595114610067578063a10d4f2f146100fa578063ac2976f6146101a5578063af89bc4314610216575b600080fd5b34801561007357600080fd5b5061009c600480360381019080803590602001909291908035906020019092919050505061027f565b6040518083815260200180602001828103825283818151815260200191508051906020019060200280838360005b838110156100e55780820151818401526020810190506100ca565b50505050905001935050505060405180910390f35b34801561010657600080fd5b50610129600480360381019080803560001916906020019092919050505061043c565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b3480156101b157600080fd5b506101d4600480360381019080803560001916906020019092919050505061053b565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34801561022257600080fd5b506102656004803603810190808035600019169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610617565b604051808215151515815260200191505060405180910390f35b60006060600061028d61084f565b6060600080871115806102a05750606487115b156102aa57600a96505b600093506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166338f1804489896040518363ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018083815260200182815260200192505050610cc060405180830381600087803b15801561034757600080fd5b505af115801561035b573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250610cc081101561038157600080fd5b81019080805190602001909291908051906020019092919091905050809550819650829850505050836040519080825280602002602001820160405280156103d85781602001602082028038833980820191505090505b509150600090505b8381101561042e5782816064811015156103f657fe5b6020020151828281518110151561040957fe5b90602001906020020190600019169081600019168152505080806001019150506103e0565b819450505050509250929050565b60008060008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663cbea41d1856040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050606060405180830381600087803b1580156104d957600080fd5b505af11580156104ed573d6000803e3d6000fd5b505050506040513d606081101561050357600080fd5b810190808051906020019092919080519060200190929190805190602001909291905050508093508194508295505050509193909250565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663ac2976f6836040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050602060405180830381600087803b1580156105d557600080fd5b505af11580156105e9573d6000803e3d6000fd5b505050506040513d60208110156105ff57600080fd5b81019080805190602001909291905050509050919050565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166308463c40846040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050602060405180830381600087803b1580156106b157600080fd5b505af11580156106c5573d6000803e3d6000fd5b505050506040513d60208110156106db57600080fd5b810190808051906020019092919050505015610734577f13c45be6a13a69b72e3af68ac93a23b440f116e2fca46ff32202473842c771066207a1846040518082815260200191505060405180910390a160009050610849565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16636d33e1c88484426040518463ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018084600019166000191681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019350505050602060405180830381600087803b15801561080857600080fd5b505af115801561081c573d6000803e3d6000fd5b505050506040513d602081101561083257600080fd5b810190808051906020019092919050505050600190505b92915050565b610c80604051908101604052806064906020820280388339808201915050905050905600a165627a7a72305820aa84f60bcccd5733165047a23d1d16271177dd2e0af33dc733c747725a977c570029";

    public static final String FUNC_LISTTOPICNAME = "listTopicName";

    public static final String FUNC_GETTOPICINFO = "getTopicInfo";

    public static final String FUNC_GETTOPICADDRESS = "getTopicAddress";

    public static final String FUNC_ADDTOPICINFO = "addTopicInfo";

    public static final Event LOGADDTOPICNAMEADDRESS_EVENT = new Event("LogAddTopicNameAddress",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
    ;

    @Deprecated
    protected TopicController(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TopicController(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TopicController(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TopicController(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<Tuple2<BigInteger, List<byte[]>>> listTopicName(BigInteger pageIndex, BigInteger pageSize) {
        final Function function = new Function(FUNC_LISTTOPICNAME,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageIndex),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageSize)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<DynamicArray<Bytes32>>() {
                }));
        return new RemoteCall<Tuple2<BigInteger, List<byte[]>>>(
                new Callable<Tuple2<BigInteger, List<byte[]>>>() {
                    @Override
                    public Tuple2<BigInteger, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, List<byte[]>>(
                                (BigInteger) results.get(0).getValue(),
                                convertToNative((List<Bytes32>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteCall<Tuple3<String, String, BigInteger>> getTopicInfo(byte[] topicName) {
        final Function function = new Function(FUNC_GETTOPICINFO,
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

    public RemoteCall<String> getTopicAddress(byte[] topicName) {
        final Function function = new Function(FUNC_GETTOPICADDRESS,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addTopicInfo(byte[] topicName, String topicAddress) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void addTopicInfo(byte[] topicName, String topicAddress, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public List<LogAddTopicNameAddressEventResponse> getLogAddTopicNameAddressEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(LOGADDTOPICNAMEADDRESS_EVENT, transactionReceipt);
        ArrayList<LogAddTopicNameAddressEventResponse> responses = new ArrayList<LogAddTopicNameAddressEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            LogAddTopicNameAddressEventResponse typedResponse = new LogAddTopicNameAddressEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.retCode = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<LogAddTopicNameAddressEventResponse> logAddTopicNameAddressEventFlowable(BcosFilter filter) {
        return web3j.logFlowable(filter).map(new io.reactivex.functions.Function<Log, LogAddTopicNameAddressEventResponse>() {
            @Override
            public LogAddTopicNameAddressEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(LOGADDTOPICNAMEADDRESS_EVENT, log);
                LogAddTopicNameAddressEventResponse typedResponse = new LogAddTopicNameAddressEventResponse();
                typedResponse.log = log;
                typedResponse.retCode = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<LogAddTopicNameAddressEventResponse> logAddTopicNameAddressEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        BcosFilter filter = new BcosFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(LOGADDTOPICNAMEADDRESS_EVENT));
        return logAddTopicNameAddressEventFlowable(filter);
    }

    @Deprecated
    public static TopicController load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TopicController load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TopicController load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new TopicController(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TopicController load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TopicController(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TopicController> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String topicDataAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicDataAddress)));
        return deployRemoteCall(TopicController.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<TopicController> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String topicDataAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicDataAddress)));
        return deployRemoteCall(TopicController.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TopicController> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String topicDataAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicDataAddress)));
        return deployRemoteCall(TopicController.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TopicController> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String topicDataAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicDataAddress)));
        return deployRemoteCall(TopicController.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class LogAddTopicNameAddressEventResponse {
        public Log log;

        public BigInteger retCode;
    }
}
