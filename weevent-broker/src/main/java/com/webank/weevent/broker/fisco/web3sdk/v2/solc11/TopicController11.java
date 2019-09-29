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
    public static String BINARY = "608060405234801561001057600080fd5b5060405160208061178d833981018060405261002f9190810190610089565b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550506100d2565b600061008182516100b2565b905092915050565b60006020828403121561009b57600080fd5b60006100a984828501610075565b91505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6116ac806100e16000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630db5c5421461007257806325bc59511461009d5780636741129c146100dc578063912849fe14610105578063f713322214610149575b600080fd5b34801561007e57600080fd5b50610087610186565b60405161009491906112b3565b60405180910390f35b3480156100a957600080fd5b506100c460048036036100bf9190810190611057565b6101af565b6040516100d3939291906113ff565b60405180910390f35b3480156100e857600080fd5b5061010360048036036100fe9190810190610ea4565b61035c565b005b34801561011157600080fd5b5061012c60048036036101279190810190611016565b6106e5565b60405161014098979695949392919061135f565b60405180910390f35b34801561015557600080fd5b50610170600480360361016b9190810190611016565b6108e7565b60405161017d9190611344565b60405180910390f35b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b600080606060006060600080871115806101c95750606487115b156101d357600a96505b60028054905095508688029250826002805490501115156101f75760009450610352565b8683016002805490501015610213578260028054905003610215565b865b94508460405190808252806020026020018201604052801561024b57816020015b60608152602001906001900390816102365790505b509150600090505b8681101561034e576002805490508310151561026e5761034e565b60028381548110151561027d57fe5b906000526020600020018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561031b5780601f106102f05761010080835404028352916020019161031b565b820191906000526020600020905b8154815290600101906020018083116102fe57829003601f168201915b5050505050828281518110151561032e57fe5b906020019060200201819052506001830192508080600101915050610253565b8193505b5050509250925092565b60006060610368610b4b565b600092505b8a51831015610628578a8381518110151561038457fe5b9060200190602002015191506001826040518082805190602001908083835b6020831015156103c857805182526020820191506020810190506020830392506103a3565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160018201548152602001600282015481525050905080602001516000141561061b57898381518110151561048e57fe5b90602001906020020151816000019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff168152505088838151811015156104dd57fe5b9060200190602002015181602001818152505087838151811015156104fe57fe5b90602001906020020151816040018181525050806001836040518082805190602001908083835b60208310151561054a5780518252602082019150602081019050602083039250610525565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010155604082015181600201559050506002829080600181540180825580915050906001820390600052602060002001600090919290919091509080519060200190610618929190610b83565b50505b828060010193505061036d565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16632b51c66b8c898989896040518663ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016106a69594939291906112ce565b600060405180830381600087803b1580156106c057600080fd5b505af11580156106d4573d6000803e3d6000fd5b505050505050505050505050505050565b6000806000806000806000806106f9610b4b565b60018a6040518082805190602001908083835b602083101515610731578051825260208201915060208101905060208303925061070c565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182015481526020016002820154815250509050806020015160001415985088156108db578060000151975080602001519650806040015195506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663c4c083608b6040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040161087891906113dd565b608060405180830381600087803b15801561089257600080fd5b505af11580156108a6573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052506108ca9190810190611093565b809550819650829750839850505050505b50919395975091939597565b60006108f1610b4b565b6001836040518082805190602001908083835b6020831015156109295780518252602082019150602081019050602083039250610904565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020606060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182015481526020016002820154815250509050806020015160001415156109eb5760009150610b45565b32816000019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff16815250504281602001818152505043816040018181525050806001846040518082805190602001908083835b602083101515610a705780518252602082019150602081019050602083039250610a4b565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010155604082015181600201559050506002839080600181540180825580915050906001820390600052602060002001600090919290919091509080519060200190610b3e929190610b83565b5050600191505b50919050565b606060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff16815260200160008152602001600081525090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610bc457805160ff1916838001178555610bf2565b82800160010185558215610bf2579182015b82811115610bf1578251825591602001919060010190610bd6565b5b509050610bff9190610c03565b5090565b610c2591905b80821115610c21576000816000905550600101610c09565b5090565b90565b6000610c3482356115f5565b905092915050565b6000610c4882516115f5565b905092915050565b600082601f8301121515610c6357600080fd5b8135610c76610c718261146a565b61143d565b91508181835260208401935060208101905083856020840282011115610c9b57600080fd5b60005b83811015610ccb5781610cb18882610c28565b845260208401935060208301925050600181019050610c9e565b5050505092915050565b600082601f8301121515610ce857600080fd5b8135610cfb610cf682611492565b61143d565b9150818183526020840193506020810190508360005b83811015610d415781358601610d278882610dd0565b845260208401935060208301925050600181019050610d11565b5050505092915050565b600082601f8301121515610d5e57600080fd5b8135610d71610d6c826114ba565b61143d565b91508181835260208401935060208101905083856020840282011115610d9657600080fd5b60005b83811015610dc65781610dac8882610e7c565b845260208401935060208301925050600181019050610d99565b5050505092915050565b600082601f8301121515610de357600080fd5b8135610df6610df1826114e2565b61143d565b91508082526020830160208301858383011115610e1257600080fd5b610e1d83828461161f565b50505092915050565b600082601f8301121515610e3957600080fd5b8135610e4c610e478261150e565b61143d565b91508082526020830160208301858383011115610e6857600080fd5b610e7383828461161f565b50505092915050565b6000610e888235611615565b905092915050565b6000610e9c8251611615565b905092915050565b600080600080600080600080610100898b031215610ec157600080fd5b600089013567ffffffffffffffff811115610edb57600080fd5b610ee78b828c01610cd5565b985050602089013567ffffffffffffffff811115610f0457600080fd5b610f108b828c01610c50565b975050604089013567ffffffffffffffff811115610f2d57600080fd5b610f398b828c01610d4b565b965050606089013567ffffffffffffffff811115610f5657600080fd5b610f628b828c01610d4b565b955050608089013567ffffffffffffffff811115610f7f57600080fd5b610f8b8b828c01610d4b565b94505060a089013567ffffffffffffffff811115610fa857600080fd5b610fb48b828c01610d4b565b93505060c089013567ffffffffffffffff811115610fd157600080fd5b610fdd8b828c01610d4b565b92505060e089013567ffffffffffffffff811115610ffa57600080fd5b6110068b828c01610c50565b9150509295985092959890939650565b60006020828403121561102857600080fd5b600082013567ffffffffffffffff81111561104257600080fd5b61104e84828501610e26565b91505092915050565b6000806040838503121561106a57600080fd5b600061107885828601610e7c565b925050602061108985828601610e7c565b9150509250929050565b600080600080608085870312156110a957600080fd5b60006110b787828801610e90565b94505060206110c887828801610e90565b93505060406110d987828801610e90565b92505060606110ea87828801610c3c565b91505092959194509250565b6110ff816115bf565b82525050565b600061111082611561565b8084526020840193506111228361153a565b60005b82811015611154576111388683516110f6565b61114182611598565b9150602086019550600181019050611125565b50849250505092915050565b600061116b8261156c565b8084526020840193508360208202850161118485611547565b60005b848110156111bd57838303885261119f83835161126e565b92506111aa826115a5565b9150602088019750600181019050611187565b508196508694505050505092915050565b60006111d982611577565b8084526020840193506111eb83611554565b60005b8281101561121d576112018683516112a4565b61120a826115b2565b91506020860195506001810190506111ee565b50849250505092915050565b611232816115df565b82525050565b60006112438261158d565b80845261125781602086016020860161162e565b61126081611661565b602085010191505092915050565b600061127982611582565b80845261128d81602086016020860161162e565b61129681611661565b602085010191505092915050565b6112ad816115eb565b82525050565b60006020820190506112c860008301846110f6565b92915050565b600060a08201905081810360008301526112e88188611160565b905081810360208301526112fc81876111ce565b9050818103604083015261131081866111ce565b9050818103606083015261132481856111ce565b905081810360808301526113388184611105565b90509695505050505050565b60006020820190506113596000830184611229565b92915050565b600061010082019050611375600083018b611229565b611382602083018a6110f6565b61138f60408301896112a4565b61139c60608301886112a4565b6113a960808301876112a4565b6113b660a08301866112a4565b6113c360c08301856112a4565b6113d060e08301846110f6565b9998505050505050505050565b600060208201905081810360008301526113f78184611238565b905092915050565b600060608201905061141460008301866112a4565b61142160208301856112a4565b81810360408301526114338184611160565b9050949350505050565b6000604051905081810181811067ffffffffffffffff8211171561146057600080fd5b8060405250919050565b600067ffffffffffffffff82111561148157600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156114a957600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156114d157600080fd5b602082029050602081019050919050565b600067ffffffffffffffff8211156114f957600080fd5b601f19601f8301169050602081019050919050565b600067ffffffffffffffff82111561152557600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b6000602082019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b600081519050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b6000602082019050919050565b6000602082019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60008115159050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b82818337600083830152505050565b60005b8381101561164c578082015181840152602081019050611631565b8381111561165b576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a723058209ccfe05bef9a62e861663d855c71c5c591e6a12b16dcf2a7e7f9f8c30ace24676c6578706572696d656e74616cf50037";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[],\"name\":\"getTopicAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"pageIndex\",\"type\":\"uint256\"},{\"name\":\"pageSize\",\"type\":\"uint256\"}],\"name\":\"listTopicName\",\"outputs\":[{\"name\":\"total\",\"type\":\"uint256\"},{\"name\":\"size\",\"type\":\"uint256\"},{\"name\":\"topics\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string[]\"},{\"name\":\"topicSender\",\"type\":\"address[]\"},{\"name\":\"topicTimestamp\",\"type\":\"uint256[]\"},{\"name\":\"topicBlock\",\"type\":\"uint256[]\"},{\"name\":\"lastSequence\",\"type\":\"uint256[]\"},{\"name\":\"lastBlock\",\"type\":\"uint256[]\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256[]\"},{\"name\":\"lastSender\",\"type\":\"address[]\"}],\"name\":\"flushTopicInfo\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getTopicInfo\",\"outputs\":[{\"name\":\"exist\",\"type\":\"bool\"},{\"name\":\"topicSender\",\"type\":\"address\"},{\"name\":\"topicTimestamp\",\"type\":\"uint256\"},{\"name\":\"topicBlock\",\"type\":\"uint256\"},{\"name\":\"lastSequence\",\"type\":\"uint256\"},{\"name\":\"lastBlock\",\"type\":\"uint256\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256\"},{\"name\":\"lastSender\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"addTopicInfo\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"topicAddress\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";

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
                }, new TypeReference<DynamicArray<Utf8String>>() {
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
