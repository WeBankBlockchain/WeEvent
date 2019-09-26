package com.webank.weevent.broker.fisco.web3sdk.v2.solc11;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bool;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.StaticArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple8;
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
    public static String BINARY = "608060405234801561001057600080fd5b50604051602080610e06833981018060405261002f9190810190610089565b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550506100d2565b600061008182516100b2565b905092915050565b60006020828403121561009b57600080fd5b60006100a984828501610075565b91505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b610d25806100e16000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630db5c5421461006757806325bc595114610092578063912849fe146100d1578063f713322214610115575b600080fd5b34801561007357600080fd5b5061007c610152565b6040516100899190610a93565b60405180910390f35b34801561009e57600080fd5b506100b960048036036100b491908101906108f6565b61017b565b6040516100c893929190610b69565b60405180910390f35b3480156100dd57600080fd5b506100f860048036036100f391908101906108b5565b6102b7565b60405161010c989796959493929190610ac9565b60405180910390f35b34801561012157600080fd5b5061013c600480360361013791908101906108b5565b6104b9565b6040516101499190610aae565b60405180910390f35b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b60008061018661071d565b6000806000861115806101995750606486115b156101a357600a95505b8587029150600090505b858110156102a357600280549050821015156101c8576102a3565b6002828154811015156101d757fe5b906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102755780601f1061024a57610100808354040283529160200191610275565b820191906000526020600020905b81548152906001019060200180831161025857829003601f168201915b5050505050838260648110151561028857fe5b602002018190525060018201915080806001019150506101ad565b600280549050945080935050509250925092565b6000806000806000806000806102cb610746565b60018a6040518082805190602001908083835b60208310151561030357805182526020820191506020810190506020830392506102de565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182015481526020016002820154815250509050806020015160001415985088156104ad578060000151975080602001519650806040015195506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663c4c083608b6040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040161044a9190610b47565b608060405180830381600087803b15801561046457600080fd5b505af1158015610478573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061049c9190810190610932565b809550819650829750839850505050505b50919395975091939597565b60006104c3610746565b6001836040518082805190602001908083835b6020831015156104fb57805182526020820191506020810190506020830392506104d6565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182015481526020016002820154815250509050806020015160001415156105bd5760009150610717565b32816000019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff16815250504281602001818152505043816040018181525050806001846040518082805190602001908083835b602083101515610642578051825260208201915060208101905060208303925061061d565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001015560408201518160020155905050600283908060018154018082558091505090600182039060005260206000200160009091929091909150908051906020019061071092919061077e565b5050600191505b50919050565b610c80604051908101604052806064905b606081526020019060019003908161072e5790505090565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff16815260200160008152602001600081525090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106107bf57805160ff19168380011785556107ed565b828001600101855582156107ed579182015b828111156107ec5782518255916020019190600101906107d1565b5b5090506107fa91906107fe565b5090565b61082091905b8082111561081c576000816000905550600101610804565b5090565b90565b600061082f8251610c6e565b905092915050565b600082601f830112151561084a57600080fd5b813561085d61085882610bd4565b610ba7565b9150808252602083016020830185838301111561087957600080fd5b610884838284610c98565b50505092915050565b60006108998235610c8e565b905092915050565b60006108ad8251610c8e565b905092915050565b6000602082840312156108c757600080fd5b600082013567ffffffffffffffff8111156108e157600080fd5b6108ed84828501610837565b91505092915050565b6000806040838503121561090957600080fd5b60006109178582860161088d565b92505060206109288582860161088d565b9150509250929050565b6000806000806080858703121561094857600080fd5b6000610956878288016108a1565b9450506020610967878288016108a1565b9350506040610978878288016108a1565b925050606061098987828801610823565b91505092959194509250565b61099e81610c38565b82525050565b60006109af82610c0a565b836020820285016109bf85610c00565b60005b848110156109f85783830388526109da838351610a4e565b92506109e582610c2b565b91506020880197506001810190506109c2565b508196508694505050505092915050565b610a1281610c58565b82525050565b6000610a2382610c20565b808452610a37816020860160208601610ca7565b610a4081610cda565b602085010191505092915050565b6000610a5982610c15565b808452610a6d816020860160208601610ca7565b610a7681610cda565b602085010191505092915050565b610a8d81610c64565b82525050565b6000602082019050610aa86000830184610995565b92915050565b6000602082019050610ac36000830184610a09565b92915050565b600061010082019050610adf600083018b610a09565b610aec602083018a610995565b610af96040830189610a84565b610b066060830188610a84565b610b136080830187610a84565b610b2060a0830186610a84565b610b2d60c0830185610a84565b610b3a60e0830184610995565b9998505050505050505050565b60006020820190508181036000830152610b618184610a18565b905092915050565b6000606082019050610b7e6000830186610a84565b610b8b6020830185610a84565b8181036040830152610b9d81846109a4565b9050949350505050565b6000604051905081810181811067ffffffffffffffff82111715610bca57600080fd5b8060405250919050565b600067ffffffffffffffff821115610beb57600080fd5b601f19601f8301169050602081019050919050565b6000819050919050565b600060649050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60008115159050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b82818337600083830152505050565b60005b83811015610cc5578082015181840152602081019050610caa565b83811115610cd4576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820ea91d224645a43d805c020b3004eb1c1620d51131a8c02a2013db4ece3f84ed76c6578706572696d656e74616cf50037";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopicName\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"size\",\"type\":\"uint256\"},{\"name\":\"topics\",\"type\":\"string[100]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopicInfo\",\"outputs\":[{\"name\":\"exist\",\"type\":\"bool\"},{\"name\":\"sender\",\"type\":\"address\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"block\",\"type\":\"uint256\"},{\"name\":\"lastSequence\",\"type\":\"uint256\"},{\"name\":\"lastBlock\",\"type\":\"uint256\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256\"},{\"name\":\"lastSender\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"addTopicInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";

    public static final String FUNC_GETTOPICADDRESS = "getTopicAddress";

    public static final String FUNC_LISTTOPICNAME = "listTopicName";

    public static final String FUNC_GETTOPICINFO = "getTopicInfo";

    public static final String FUNC_ADDTOPICINFO = "addTopicInfo";

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

    public RemoteCall<String> getTopicAddress() {
        final Function function = new Function(FUNC_GETTOPICADDRESS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<Tuple3<BigInteger, BigInteger, List<String>>> listTopicName(BigInteger pageIndex, BigInteger pageSize) {
        final Function function = new Function(FUNC_LISTTOPICNAME,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageIndex),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(pageSize)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<StaticArray<Utf8String>>() {
                }));
        return new RemoteCall<Tuple3<BigInteger, BigInteger, List<String>>>(
                new Callable<Tuple3<BigInteger, BigInteger, List<String>>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, List<String>>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                convertToNative((List<Utf8String>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteCall<Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String>> getTopicInfo(String topicName) {
        final Function function = new Function(FUNC_GETTOPICINFO,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }, new TypeReference<Address>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Address>() {
                }));
        return new RemoteCall<Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String>>(
                new Callable<Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple8<Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String>(
                                (Boolean) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue(),
                                (BigInteger) results.get(3).getValue(),
                                (BigInteger) results.get(4).getValue(),
                                (BigInteger) results.get(5).getValue(),
                                (BigInteger) results.get(6).getValue(),
                                (String) results.get(7).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> addTopicInfo(String topicName) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void addTopicInfo(String topicName, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String addTopicInfoSeq(String topicName) {
        final Function function = new Function(
                FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple1<String> getAddTopicInfoInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<String>(

                (String) results.get(0).getValue()
        );
    }

    public Tuple1<Boolean> getAddTopicInfoOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_ADDTOPICINFO,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
        );
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

    public static RemoteCall<TopicController> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<TopicController> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TopicController> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TopicController> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }
}
