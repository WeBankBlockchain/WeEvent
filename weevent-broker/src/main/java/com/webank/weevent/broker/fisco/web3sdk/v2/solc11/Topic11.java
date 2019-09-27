package com.webank.weevent.broker.fisco.web3sdk.v2.solc11;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
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
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.TransactionManager;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("unchecked")
public class Topic11 extends Contract {
    public static String BINARY = "608060405234801561001057600080fd5b50610d96806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632b51c66b1461005c578063a99077f214610085578063c4c08360146100c2575b600080fd5b34801561006857600080fd5b50610083600480360361007e91908101906109b7565b610102565b005b34801561009157600080fd5b506100ac60048036036100a79190810190610ae7565b6103ce565b6040516100b99190610b9c565b60405180910390f35b3480156100ce57600080fd5b506100e960048036036100e49190810190610aa6565b610605565b6040516100f99493929190610bb7565b60405180910390f35b6000606061010e610724565b600092505b87518310156103c457878381518110151561012a57fe5b9060200190602002015191506000826040518082805190602001908083835b60208310151561016e5780518252602082019150602081019050602083039250610149565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152505090508060400151600014156103b757868381518110151561023e57fe5b90602001906020020151816000018181525050858381518110151561025f57fe5b90602001906020020151816020018181525050848381518110151561028057fe5b9060200190602002015181604001818152505083838151811015156102a157fe5b90602001906020020151816060019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff1681525050806000836040518082805190602001908083835b60208310151561031b57805182526020820191506020810190506020830392506102f6565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055509050505b8280600101935050610113565b5050505050505050565b60006103d8610724565b6000856040518082805190602001908083835b60208310151561041057805182526020820191506020810190506020830392506103eb565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152505090506001816000015101816000018181525050438160200181815250504281604001818152505032816060019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff1681525050806000866040518082805190602001908083835b60208310151561055b5780518252602082019150602081019050602083039250610536565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555090505080600001519150509392505050565b600080600080610613610724565b6000866040518082805190602001908083835b60208310151561064b5780518252602082019150602081019050602083039250610626565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681525050905080600001519450806020015193508060400151925080606001519150509193509193565b608060405190810160405280600081526020016000815260200160008152602001600073ffffffffffffffffffffffffffffffffffffffff1681525090565b600061076f8235610d23565b905092915050565b600082601f830112151561078a57600080fd5b813561079d61079882610c29565b610bfc565b915081818352602084019350602081019050838560208402820111156107c257600080fd5b60005b838110156107f257816107d88882610763565b8452602084019350602083019250506001810190506107c5565b5050505092915050565b600082601f830112151561080f57600080fd5b813561082261081d82610c51565b610bfc565b9150818183526020840193506020810190508360005b83811015610868578135860161084e88826108f7565b845260208401935060208301925050600181019050610838565b5050505092915050565b600082601f830112151561088557600080fd5b813561089861089382610c79565b610bfc565b915081818352602084019350602081019050838560208402820111156108bd57600080fd5b60005b838110156108ed57816108d388826109a3565b8452602084019350602083019250506001810190506108c0565b5050505092915050565b600082601f830112151561090a57600080fd5b813561091d61091882610ca1565b610bfc565b9150808252602083016020830185838301111561093957600080fd5b610944838284610d4d565b50505092915050565b600082601f830112151561096057600080fd5b813561097361096e82610ccd565b610bfc565b9150808252602083016020830185838301111561098f57600080fd5b61099a838284610d4d565b50505092915050565b60006109af8235610d43565b905092915050565b600080600080600060a086880312156109cf57600080fd5b600086013567ffffffffffffffff8111156109e957600080fd5b6109f5888289016107fc565b955050602086013567ffffffffffffffff811115610a1257600080fd5b610a1e88828901610872565b945050604086013567ffffffffffffffff811115610a3b57600080fd5b610a4788828901610872565b935050606086013567ffffffffffffffff811115610a6457600080fd5b610a7088828901610872565b925050608086013567ffffffffffffffff811115610a8d57600080fd5b610a9988828901610777565b9150509295509295909350565b600060208284031215610ab857600080fd5b600082013567ffffffffffffffff811115610ad257600080fd5b610ade8482850161094d565b91505092915050565b600080600060608486031215610afc57600080fd5b600084013567ffffffffffffffff811115610b1657600080fd5b610b228682870161094d565b935050602084013567ffffffffffffffff811115610b3f57600080fd5b610b4b8682870161094d565b925050604084013567ffffffffffffffff811115610b6857600080fd5b610b748682870161094d565b9150509250925092565b610b8781610cf9565b82525050565b610b9681610d19565b82525050565b6000602082019050610bb16000830184610b8d565b92915050565b6000608082019050610bcc6000830187610b8d565b610bd96020830186610b8d565b610be66040830185610b8d565b610bf36060830184610b7e565b95945050505050565b6000604051905081810181811067ffffffffffffffff82111715610c1f57600080fd5b8060405250919050565b600067ffffffffffffffff821115610c4057600080fd5b602082029050602081019050919050565b600067ffffffffffffffff821115610c6857600080fd5b602082029050602081019050919050565b600067ffffffffffffffff821115610c9057600080fd5b602082029050602081019050919050565b600067ffffffffffffffff821115610cb857600080fd5b601f19601f8301169050602081019050919050565b600067ffffffffffffffff821115610ce457600080fd5b601f19601f8301169050602081019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b828183376000838301525050505600a265627a7a723058200472506371dbb32a36fb72b012310f555e4179c1ecc0cc19179666b7735a4d056c6578706572696d656e74616cf50037";

    public static final String ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string[]\"},{\"name\":\"lastSequence\",\"type\":\"uint256[]\"},{\"name\":\"lastBlock\",\"type\":\"uint256[]\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256[]\"},{\"name\":\"lastSender\",\"type\":\"address[]\"}],\"name\":\"flushSnapshot\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"eventContent\",\"type\":\"string\"},{\"name\":\"extensions\",\"type\":\"string\"}],\"name\":\"publishWeEvent\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getSnapshot\",\"outputs\":[{\"name\":\"lastSequence\",\"type\":\"uint256\"},{\"name\":\"lastBlock\",\"type\":\"uint256\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256\"},{\"name\":\"lastSender\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

    public static final TransactionDecoder transactionDecoder = new TransactionDecoder(ABI, BINARY);

    public static final String FUNC_FLUSHSNAPSHOT = "flushSnapshot";

    public static final String FUNC_PUBLISHWEEVENT = "publishWeEvent";

    public static final String FUNC_GETSNAPSHOT = "getSnapshot";

    @Deprecated
    protected Topic11(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Topic11(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Topic11(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Topic11(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static TransactionDecoder getTransactionDecoder() {
        return transactionDecoder;
    }

    public RemoteCall<TransactionReceipt> flushSnapshot(List<String> topicName, List<BigInteger> lastSequence, List<BigInteger> lastBlock, List<BigInteger> lastTimestamp, List<String> lastSender) {
        final Function function = new Function(
                FUNC_FLUSHSNAPSHOT,
                Arrays.<Type>asList(topicName.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("string[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicName, org.fisco.bcos.web3j.abi.datatypes.Utf8String.class)),
                        lastSequence.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastSequence, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastBlock.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastBlock, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastTimestamp.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastTimestamp, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastSender.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("address[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastSender, org.fisco.bcos.web3j.abi.datatypes.Address.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void flushSnapshot(List<String> topicName, List<BigInteger> lastSequence, List<BigInteger> lastBlock, List<BigInteger> lastTimestamp, List<String> lastSender, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_FLUSHSNAPSHOT,
                Arrays.<Type>asList(topicName.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("string[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicName, org.fisco.bcos.web3j.abi.datatypes.Utf8String.class)),
                        lastSequence.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastSequence, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastBlock.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastBlock, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastTimestamp.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastTimestamp, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastSender.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("address[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastSender, org.fisco.bcos.web3j.abi.datatypes.Address.class))),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String flushSnapshotSeq(List<String> topicName, List<BigInteger> lastSequence, List<BigInteger> lastBlock, List<BigInteger> lastTimestamp, List<String> lastSender) {
        final Function function = new Function(
                FUNC_FLUSHSNAPSHOT,
                Arrays.<Type>asList(topicName.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("string[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicName, org.fisco.bcos.web3j.abi.datatypes.Utf8String.class)),
                        lastSequence.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastSequence, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastBlock.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastBlock, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastTimestamp.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastTimestamp, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        lastSender.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("address[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(lastSender, org.fisco.bcos.web3j.abi.datatypes.Address.class))),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple5<List<String>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<String>> getFlushSnapshotInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_FLUSHSNAPSHOT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Utf8String>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Address>>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple5<List<String>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<String>>(

                convertToNative((List<Utf8String>) results.get(0).getValue()),
                convertToNative((List<Uint256>) results.get(1).getValue()),
                convertToNative((List<Uint256>) results.get(2).getValue()),
                convertToNative((List<Uint256>) results.get(3).getValue()),
                convertToNative((List<Address>) results.get(4).getValue())
        );
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
    public static Topic11 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic11(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Topic11 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Topic11(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Topic11 load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Topic11(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Topic11 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Topic11(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Topic11> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Topic11.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Topic11> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Topic11.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Topic11> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Topic11.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Topic11> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Topic11.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
