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
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
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
public class TopicController11 extends Contract {
    public static String BINARY = "608060405234801561001057600080fd5b506040516020806117cc833981018060405261002f9190810190610089565b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550506100d2565b600061008182516100b2565b905092915050565b60006020828403121561009b57600080fd5b60006100a984828501610075565b91505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6116eb806100e16000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630db5c5421461007257806325bc59511461009d5780636741129c146100dc578063912849fe14610105578063f713322214610149575b600080fd5b34801561007e57600080fd5b50610087610186565b60405161009491906112d0565b60405180910390f35b3480156100a957600080fd5b506100c460048036036100bf919081019061100f565b6101af565b6040516100d39392919061141c565b60405180910390f35b3480156100e857600080fd5b5061010360048036036100fe9190810190610e5c565b6102eb565b005b34801561011157600080fd5b5061012c60048036036101279190810190610fce565b610674565b60405161014098979695949392919061137c565b60405180910390f35b34801561015557600080fd5b50610170600480360361016b9190810190610fce565b610876565b60405161017d9190611361565b60405180910390f35b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6000806101ba610ada565b6000806000861115806101cd5750606486115b156101d757600a95505b8587029150600090505b858110156102d757600280549050821015156101fc576102d7565b60028281548110151561020b57fe5b906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102a95780601f1061027e576101008083540402835291602001916102a9565b820191906000526020600020905b81548152906001019060200180831161028c57829003601f168201915b505050505083826064811015156102bc57fe5b602002018190525060018201915080806001019150506101e1565b600280549050945080935050509250925092565b600060606102f7610b03565b600092505b8a518310156105b7578a8381518110151561031357fe5b9060200190602002015191506001826040518082805190602001908083835b6020831015156103575780518252602082019150602081019050602083039250610332565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820154815260200160028201548152505090508060200151600014156105aa57898381518110151561041d57fe5b90602001906020020151816000019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff1681525050888381518110151561046c57fe5b90602001906020020151816020018181525050878381518110151561048d57fe5b90602001906020020151816040018181525050806001836040518082805190602001908083835b6020831015156104d957805182526020820191506020810190506020830392506104b4565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550602082015181600101556040820151816002015590505060028290806001815401808255809150509060018203906000526020600020016000909192909190915090805190602001906105a7929190610b3b565b50505b82806001019350506102fc565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16632b51c66b8c898989896040518663ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016106359594939291906112eb565b600060405180830381600087803b15801561064f57600080fd5b505af1158015610663573d6000803e3d6000fd5b505050505050505050505050505050565b600080600080600080600080610688610b03565b60018a6040518082805190602001908083835b6020831015156106c0578051825260208201915060208101905060208303925061069b565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820154815260200160028201548152505090508060200151600014159850881561086a578060000151975080602001519650806040015195506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663c4c083608b6040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040161080791906113fa565b608060405180830381600087803b15801561082157600080fd5b505af1158015610835573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250610859919081019061104b565b809550819650829750839850505050505b50919395975091939597565b6000610880610b03565b6001836040518082805190602001908083835b6020831015156108b85780518252602082019150602081019050602083039250610893565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820154815260200160028201548152505090508060200151600014151561097a5760009150610ad4565b32816000019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff16815250504281602001818152505043816040018181525050806001846040518082805190602001908083835b6020831015156109ff57805182526020820191506020810190506020830392506109da565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010155604082015181600201559050506002839080600181540180825580915050906001820390600052602060002001600090919290919091509080519060200190610acd929190610b3b565b5050600191505b50919050565b610c80604051908101604052806064905b6060815260200190600190039081610aeb5790505090565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff16815260200160008152602001600081525090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610b7c57805160ff1916838001178555610baa565b82800160010185558215610baa579182015b82811115610ba9578251825591602001919060010190610b8e565b5b509050610bb79190610bbb565b5090565b610bdd91905b80821115610bd9576000816000905550600101610bc1565b5090565b90565b6000610bec8235611634565b905092915050565b6000610c008251611634565b905092915050565b600082601f8301121515610c1b57600080fd5b8135610c2e610c2982611487565b61145a565b91508181835260208401935060208101905083856020840282011115610c5357600080fd5b60005b83811015610c835781610c698882610be0565b845260208401935060208301925050600181019050610c56565b5050505092915050565b600082601f8301121515610ca057600080fd5b8135610cb3610cae826114af565b61145a565b9150818183526020840193506020810190508360005b83811015610cf95781358601610cdf8882610d88565b845260208401935060208301925050600181019050610cc9565b5050505092915050565b600082601f8301121515610d1657600080fd5b8135610d29610d24826114d7565b61145a565b91508181835260208401935060208101905083856020840282011115610d4e57600080fd5b60005b83811015610d7e5781610d648882610e34565b845260208401935060208301925050600181019050610d51565b5050505092915050565b600082601f8301121515610d9b57600080fd5b8135610dae610da9826114ff565b61145a565b91508082526020830160208301858383011115610dca57600080fd5b610dd583828461165e565b50505092915050565b600082601f8301121515610df157600080fd5b8135610e04610dff8261152b565b61145a565b91508082526020830160208301858383011115610e2057600080fd5b610e2b83828461165e565b50505092915050565b6000610e408235611654565b905092915050565b6000610e548251611654565b905092915050565b600080600080600080600080610100898b031215610e7957600080fd5b600089013567ffffffffffffffff811115610e9357600080fd5b610e9f8b828c01610c8d565b985050602089013567ffffffffffffffff811115610ebc57600080fd5b610ec88b828c01610c08565b975050604089013567ffffffffffffffff811115610ee557600080fd5b610ef18b828c01610d03565b965050606089013567ffffffffffffffff811115610f0e57600080fd5b610f1a8b828c01610d03565b955050608089013567ffffffffffffffff811115610f3757600080fd5b610f438b828c01610d03565b94505060a089013567ffffffffffffffff811115610f6057600080fd5b610f6c8b828c01610d03565b93505060c089013567ffffffffffffffff811115610f8957600080fd5b610f958b828c01610d03565b92505060e089013567ffffffffffffffff811115610fb257600080fd5b610fbe8b828c01610c08565b9150509295985092959890939650565b600060208284031215610fe057600080fd5b600082013567ffffffffffffffff811115610ffa57600080fd5b61100684828501610dde565b91505092915050565b6000806040838503121561102257600080fd5b600061103085828601610e34565b925050602061104185828601610e34565b9150509250929050565b6000806000806080858703121561106157600080fd5b600061106f87828801610e48565b945050602061108087828801610e48565b935050604061109187828801610e48565b92505060606110a287828801610bf4565b91505092959194509250565b6110b7816115fe565b82525050565b60006110c882611588565b8084526020840193506110da83611557565b60005b8281101561110c576110f08683516110ae565b6110f9826115ca565b91506020860195506001810190506110dd565b50849250505092915050565b600061112382611593565b8360208202850161113385611564565b60005b8481101561116c57838303885261114e83835161128b565b9250611159826115d7565b9150602088019750600181019050611136565b508196508694505050505092915050565b60006111888261159e565b808452602084019350836020820285016111a18561156e565b60005b848110156111da5783830388526111bc83835161128b565b92506111c7826115e4565b91506020880197506001810190506111a4565b508196508694505050505092915050565b60006111f6826115a9565b8084526020840193506112088361157b565b60005b8281101561123a5761121e8683516112c1565b611227826115f1565b915060208601955060018101905061120b565b50849250505092915050565b61124f8161161e565b82525050565b6000611260826115bf565b80845261127481602086016020860161166d565b61127d816116a0565b602085010191505092915050565b6000611296826115b4565b8084526112aa81602086016020860161166d565b6112b3816116a0565b602085010191505092915050565b6112ca8161162a565b82525050565b60006020820190506112e560008301846110ae565b92915050565b600060a0820190508181036000830152611305818861117d565b9050818103602083015261131981876111eb565b9050818103604083015261132d81866111eb565b9050818103606083015261134181856111eb565b9050818103608083015261135581846110bd565b90509695505050505050565b60006020820190506113766000830184611246565b92915050565b600061010082019050611392600083018b611246565b61139f602083018a6110ae565b6113ac60408301896112c1565b6113b960608301886112c1565b6113c660808301876112c1565b6113d360a08301866112c1565b6113e060c08301856112c1565b6113ed60e08301846110ae565b9998505050505050505050565b600060208201905081810360008301526114148184611255565b905092915050565b600060608201905061143160008301866112c1565b61143e60208301856112c1565b81810360408301526114508184611118565b9050949350505050565b6000604051905081810181811067ffffffffffffffff8211171561147d57600080fd5b8060405250919050565b600067ffffffffffffffff82111561149e57600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156114c657600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156114ee57600080fd5b602082029050602081019050919050565b600067ffffffffffffffff82111561151657600080fd5b601f19601f8301169050602081019050919050565b600067ffffffffffffffff82111561154257600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b6000819050919050565b6000602082019050919050565b6000602082019050919050565b600081519050919050565b600060649050919050565b600081519050919050565b600081519050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b6000602082019050919050565b6000602082019050919050565b6000602082019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60008115159050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b82818337600083830152505050565b60005b8381101561168b578082015181840152602081019050611670565b8381111561169a576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a723058205d4f9308e6a7c574fa2b670f721f5b5f6471a54da529f854cdbb5a8cb0b9baaf6c6578706572696d656e74616cf50037";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopicName\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"size\",\"type\":\"uint256\"},{\"name\":\"topics\",\"type\":\"string[100]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string[]\"},{\"name\":\"topicSender\",\"type\":\"address[]\"},{\"name\":\"topicTimestamp\",\"type\":\"uint256[]\"},{\"name\":\"topicBlock\",\"type\":\"uint256[]\"},{\"name\":\"lastSequence\",\"type\":\"uint256[]\"},{\"name\":\"lastBlock\",\"type\":\"uint256[]\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256[]\"},{\"name\":\"lastSender\",\"type\":\"address[]\"}],\"name\":\"flushTopicInfo\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopicInfo\",\"outputs\":[{\"name\":\"exist\",\"type\":\"bool\"},{\"name\":\"topicSender\",\"type\":\"address\"},{\"name\":\"topicTimestamp\",\"type\":\"uint256\"},{\"name\":\"topicBlock\",\"type\":\"uint256\"},{\"name\":\"lastSequence\",\"type\":\"uint256\"},{\"name\":\"lastBlock\",\"type\":\"uint256\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256\"},{\"name\":\"lastSender\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"addTopicInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";

    public static final TransactionDecoder transactionDecoder = new TransactionDecoder(ABI, BINARY);

    public static final String FUNC_GETTOPICADDRESS = "getTopicAddress";

    public static final String FUNC_LISTTOPICNAME = "listTopicName";

    public static final String FUNC_FLUSHTOPICINFO = "flushTopicInfo";

    public static final String FUNC_GETTOPICINFO = "getTopicInfo";

    public static final String FUNC_ADDTOPICINFO = "addTopicInfo";

    @Deprecated
    protected TopicController11(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TopicController11(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TopicController11(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TopicController11(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static TransactionDecoder getTransactionDecoder() {
        return transactionDecoder;
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

    public RemoteCall<TransactionReceipt> flushTopicInfo(List<String> topicName, List<String> topicSender, List<BigInteger> topicTimestamp, List<BigInteger> topicBlock, List<BigInteger> lastSequence, List<BigInteger> lastBlock, List<BigInteger> lastTimestamp, List<String> lastSender) {
        final Function function = new Function(
                FUNC_FLUSHTOPICINFO,
                Arrays.<Type>asList(topicName.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("string[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicName, org.fisco.bcos.web3j.abi.datatypes.Utf8String.class)),
                        topicSender.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("address[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicSender, org.fisco.bcos.web3j.abi.datatypes.Address.class)),
                        topicTimestamp.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicTimestamp, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        topicBlock.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicBlock, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
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

    public void flushTopicInfo(List<String> topicName, List<String> topicSender, List<BigInteger> topicTimestamp, List<BigInteger> topicBlock, List<BigInteger> lastSequence, List<BigInteger> lastBlock, List<BigInteger> lastTimestamp, List<String> lastSender, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_FLUSHTOPICINFO,
                Arrays.<Type>asList(topicName.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("string[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicName, org.fisco.bcos.web3j.abi.datatypes.Utf8String.class)),
                        topicSender.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("address[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicSender, org.fisco.bcos.web3j.abi.datatypes.Address.class)),
                        topicTimestamp.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicTimestamp, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        topicBlock.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicBlock, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
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

    public String flushTopicInfoSeq(List<String> topicName, List<String> topicSender, List<BigInteger> topicTimestamp, List<BigInteger> topicBlock, List<BigInteger> lastSequence, List<BigInteger> lastBlock, List<BigInteger> lastTimestamp, List<String> lastSender) {
        final Function function = new Function(
                FUNC_FLUSHTOPICINFO,
                Arrays.<Type>asList(topicName.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("string[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Utf8String>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicName, org.fisco.bcos.web3j.abi.datatypes.Utf8String.class)),
                        topicSender.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("address[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.Address>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicSender, org.fisco.bcos.web3j.abi.datatypes.Address.class)),
                        topicTimestamp.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicTimestamp, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
                        topicBlock.isEmpty() ? org.fisco.bcos.web3j.abi.datatypes.DynamicArray.empty("uint256[]") : new org.fisco.bcos.web3j.abi.datatypes.DynamicArray<org.fisco.bcos.web3j.abi.datatypes.generated.Uint256>(
                                org.fisco.bcos.web3j.abi.Utils.typeMap(topicBlock, org.fisco.bcos.web3j.abi.datatypes.generated.Uint256.class)),
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

    public Tuple8<List<String>, List<String>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<String>> getFlushTopicInfoInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_FLUSHTOPICINFO,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Utf8String>>() {
                }, new TypeReference<DynamicArray<Address>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Address>>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple8<List<String>, List<String>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<BigInteger>, List<String>>(

                convertToNative((List<Utf8String>) results.get(0).getValue()),
                convertToNative((List<Address>) results.get(1).getValue()),
                convertToNative((List<Uint256>) results.get(2).getValue()),
                convertToNative((List<Uint256>) results.get(3).getValue()),
                convertToNative((List<Uint256>) results.get(4).getValue()),
                convertToNative((List<Uint256>) results.get(5).getValue()),
                convertToNative((List<Uint256>) results.get(6).getValue()),
                convertToNative((List<Address>) results.get(7).getValue())
        );
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
    public static TopicController11 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController11(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TopicController11 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TopicController11(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TopicController11 load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new TopicController11(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TopicController11 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TopicController11(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TopicController11> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController11.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<TopicController11> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController11.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TopicController11> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController11.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TopicController11> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String topicAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Address(topicAddress)));
        return deployRemoteCall(TopicController11.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }
}
