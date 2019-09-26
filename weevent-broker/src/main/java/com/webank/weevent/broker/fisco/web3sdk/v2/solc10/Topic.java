package com.webank.weevent.broker.fisco.web3sdk.v2.solc10;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
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
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("unchecked")
public class Topic extends Contract {
    public static String BINARY = "608060405234801561001057600080fd5b506106db806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063a99077f214610051578063c4c083601461008e575b600080fd5b34801561005d57600080fd5b50610078600480360361007391908101906104fa565b6100ce565b60405161008591906105af565b60405180910390f35b34801561009a57600080fd5b506100b560048036036100b091908101906104b9565b610305565b6040516100c594939291906105ca565b60405180910390f35b60006100d8610424565b6000856040518082805190602001908083835b60208310151561011057805182526020820191506020810190506020830392506100eb565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152505090506001816000015101816000018181525050438160200181815250504281604001818152505032816060019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff1681525050806000866040518082805190602001908083835b60208310151561025b5780518252602082019150602081019050602083039250610236565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555090505080600001519150509392505050565b600080600080610313610424565b6000866040518082805190602001908083835b60208310151561034b5780518252602082019150602081019050602083039250610326565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681525050905080600001519450806020015193508060400151925080606001519150509193509193565b608060405190810160405280600081526020016000815260200160008152602001600073ffffffffffffffffffffffffffffffffffffffff1681525090565b600082601f830112151561047657600080fd5b81356104896104848261063c565b61060f565b915080825260208301602083018583830111156104a557600080fd5b6104b0838284610692565b50505092915050565b6000602082840312156104cb57600080fd5b600082013567ffffffffffffffff8111156104e557600080fd5b6104f184828501610463565b91505092915050565b60008060006060848603121561050f57600080fd5b600084013567ffffffffffffffff81111561052957600080fd5b61053586828701610463565b935050602084013567ffffffffffffffff81111561055257600080fd5b61055e86828701610463565b925050604084013567ffffffffffffffff81111561057b57600080fd5b61058786828701610463565b9150509250925092565b61059a81610668565b82525050565b6105a981610688565b82525050565b60006020820190506105c460008301846105a0565b92915050565b60006080820190506105df60008301876105a0565b6105ec60208301866105a0565b6105f960408301856105a0565b6106066060830184610591565b95945050505050565b6000604051905081810181811067ffffffffffffffff8211171561063257600080fd5b8060405250919050565b600067ffffffffffffffff82111561065357600080fd5b601f19601f8301169050602081019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b828183376000838301525050505600a265627a7a72305820edbca52f40b1fd7167e28892a29719eaaec462c9f71b6aff431cdafafc750bf06c6578706572696d656e74616cf50037";

    public static final String ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"eventContent\",\"type\":\"string\"},{\"name\":\"extensions\",\"type\":\"string\"}],\"name\":\"publishWeEvent\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getSnapshot\",\"outputs\":[{\"name\":\"sequence\",\"type\":\"uint256\"},{\"name\":\"block\",\"type\":\"uint256\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"sender\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

    public static final String FUNC_PUBLISHWEEVENT = "publishWeEvent";

    public static final String FUNC_GETSNAPSHOT = "getSnapshot";

    @Deprecated
    protected Topic(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Topic(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Topic(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Topic(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> publishWeEvent(String topicName, String eventContent, String extensions) {
        final Function function = new Function(
                FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(eventContent),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(extensions)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void publishWeEvent(String topicName, String eventContent, String extensions, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(eventContent),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(extensions)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String publishWeEventSeq(String topicName, String eventContent, String extensions) {
        final Function function = new Function(
                FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(eventContent),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(extensions)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple3<String, String, String> getPublishWeEventInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Utf8String>() {
                }, new TypeReference<Utf8String>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple3<String, String, String>(

                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue()
        );
    }

    public Tuple1<BigInteger> getPublishWeEventOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public RemoteCall<Tuple4<BigInteger, BigInteger, BigInteger, String>> getSnapshot(String topicName) {
        final Function function = new Function(FUNC_GETSNAPSHOT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Address>() {
                }));
        return new RemoteCall<Tuple4<BigInteger, BigInteger, BigInteger, String>>(
                new Callable<Tuple4<BigInteger, BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, BigInteger, String>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue(),
                                (String) results.get(3).getValue());
                    }
                });
    }

    @Deprecated
    public static Topic load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Topic load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Topic load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Topic(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Topic load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Topic(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Topic> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Topic.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Topic> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Topic.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Topic> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Topic.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Topic> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Topic.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
