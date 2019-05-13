package com.webank.weevent.broker.fisco.contract.v2;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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
 *
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("unchecked")
public class TopicController extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b50604051602080610c0b83398101806040528101908080519060200190929190505050806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050610b88806100836000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806325bc595114610067578063912849fe14610142578063ae50137614610225578063fc3a2330146102ce575b600080fd5b34801561007357600080fd5b5061009c600480360381019080803590602001909291908035906020019092919050505061036f565b604051808481526020018060200180602001838103835285818151815260200191508051906020019060200280838360005b838110156100e95780820151818401526020810190506100ce565b50505050905001838103825284818151815260200191508051906020019060200280838360005b8381101561012b578082015181840152602081019050610110565b505050509050019550505050505060405180910390f35b34801561014e57600080fd5b506101a9600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192905050506105b1565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390f35b34801561023157600080fd5b5061028c600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061070d565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156102da57600080fd5b50610355600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610846565b604051808215151515815260200191505060405180910390f35b6000606080600061037e610b38565b610386610b38565b6060806000808a11158061039a575060648a115b156103a457600a99505b600095506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166338f180448c8c6040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808381526020018281526020019250505061194060405180830381600087803b15801561044157600080fd5b505af1158015610455573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061194081101561047b57600080fd5b810190808051906020019092919080519060200190929190919082610c800191905050809750819850829950839c5050505050856040519080825280602002602001820160405280156104dd5781602001602082028038833980820191505090505b5092508560405190808252806020026020018201604052801561050f5781602001602082028038833980820191505090505b509150600090505b8581101561059e57848160648110151561052d57fe5b6020020151838281518110151561054057fe5b906020019060200201906000191690816000191681525050838160648110151561056657fe5b6020020151828281518110151561057957fe5b9060200190602002019060001916908160001916815250508080600101915050610517565b8297508196505050505050509250925092565b60008060008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663474502d8856040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825283818151815260200191508051906020019080838360005b8381101561065f578082015181840152602081019050610644565b50505050905090810190601f16801561068c5780820380516001836020036101000a031916815260200191505b5092505050606060405180830381600087803b1580156106ab57600080fd5b505af11580156106bf573d6000803e3d6000fd5b505050506040513d60608110156106d557600080fd5b810190808051906020019092919080519060200190929190805190602001909291905050508093508194508295505050509193909250565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663ae501376836040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825283818151815260200191508051906020019080838360005b838110156107b857808201518184015260208101905061079d565b50505050905090810190601f1680156107e55780820380516001836020036101000a031916815260200191505b5092505050602060405180830381600087803b15801561080457600080fd5b505af1158015610818573d6000803e3d6000fd5b505050506040513d602081101561082e57600080fd5b81019080805190602001909291905050509050919050565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16636d7dbe85846040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825283818151815260200191508051906020019080838360005b838110156108f15780820151818401526020810190506108d6565b50505050905090810190601f16801561091e5780820380516001836020036101000a031916815260200191505b5092505050602060405180830381600087803b15801561093d57600080fd5b505af1158015610951573d6000803e3d6000fd5b505050506040513d602081101561096757600080fd5b8101908080519060200190929190505050156109c0577f13c45be6a13a69b72e3af68ac93a23b440f116e2fca46ff32202473842c771066207a1846040518082815260200191505060405180910390a160009050610b32565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663563eeb008484426040518463ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040180806020018473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001838152602001828103825285818151815260200191508051906020019080838360005b83811015610aa3578082015181840152602081019050610a88565b50505050905090810190601f168015610ad05780820380516001836020036101000a031916815260200191505b50945050505050602060405180830381600087803b158015610af157600080fd5b505af1158015610b05573d6000803e3d6000fd5b505050506040513d6020811015610b1b57600080fd5b810190808051906020019092919050505050600190505b92915050565b610c80604051908101604052806064906020820280388339808201915050905050905600a165627a7a72305820cdcf11c00e3b6e294b7b1d427313874672daf0f5cd0fd85ec12a9b7f87b170110029";

    public static final String FUNC_LISTTOPICNAME = "listTopicName";

    public static final String FUNC_GETTOPICINFO = "getTopicInfo";

    public static final String FUNC_GETTOPICADDRESS = "getTopicAddress";

    public static final String FUNC_ADDTOPICINFO = "addTopicInfo";

    public static final Event LOGADDTOPICNAMEADDRESS_EVENT = new Event("LogAddTopicNameAddress", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
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

    public RemoteCall<Tuple3<BigInteger, List<byte[]>, List<byte[]>>> listTopicName(BigInteger pageIndex, BigInteger pageSize) {
        final Function function = new Function(FUNC_LISTTOPICNAME, 
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageIndex), 
                new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageSize)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<Tuple3<BigInteger, List<byte[]>, List<byte[]>>>(
                new Callable<Tuple3<BigInteger, List<byte[]>, List<byte[]>>>() {
                    @Override
                    public Tuple3<BigInteger, List<byte[]>, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, List<byte[]>, List<byte[]>>(
                                (BigInteger) results.get(0).getValue(), 
                                convertToNative((List<Bytes32>) results.get(1).getValue()), 
                                convertToNative((List<Bytes32>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteCall<Tuple3<String, String, BigInteger>> getTopicInfo(String topicName) {
        final Function function = new Function(FUNC_GETTOPICINFO, 
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
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

    public RemoteCall<String> getTopicAddress(String topicName) {
        final Function function = new Function(FUNC_GETTOPICADDRESS, 
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addTopicInfo(String topicName, String topicAddress) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO, 
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName), 
                new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void addTopicInfo(String topicName, String topicAddress, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO, 
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName), 
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
