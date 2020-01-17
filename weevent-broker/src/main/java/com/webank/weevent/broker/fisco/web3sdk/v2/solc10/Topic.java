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
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
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
public class Topic extends Contract {
    public static String BINARY = "608060405234801561001057600080fd5b50611e67806100206000396000f300608060405260043610610099576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806322eecbd71461009e5780632b51c66b146100db57806350545d2f146101045780635fd6623314610141578063a99077f21461017e578063c1058705146101bb578063c4c08360146101f8578063fd13b77614610238578063fdcb057514610275575b600080fd5b3480156100aa57600080fd5b506100c560048036036100c09190810190611a7e565b6102b3565b6040516100d29190611bf1565b60405180910390f35b3480156100e757600080fd5b5061010260048036036100fd919081019061194e565b6104e2565b005b34801561011057600080fd5b5061012b60048036036101269190810190611a7e565b6107ae565b6040516101389190611c0c565b60405180910390f35b34801561014d57600080fd5b5061016860048036036101639190810190611a3d565b6108c1565b6040516101759190611bf1565b60405180910390f35b34801561018a57600080fd5b506101a560048036036101a09190810190611ad2565b6108d4565b6040516101b29190611c0c565b60405180910390f35b3480156101c757600080fd5b506101e260048036036101dd9190810190611a7e565b610b25565b6040516101ef9190611c0c565b60405180910390f35b34801561020457600080fd5b5061021f600480360361021a9190810190611a3d565b610fa0565b60405161022f9493929190611c57565b60405180910390f35b34801561024457600080fd5b5061025f600480360361025a9190810190611a7e565b6110bf565b60405161026c9190611bf1565b60405180910390f35b34801561028157600080fd5b5061029c60048036036102979190810190611a3d565b6112b4565b6040516102aa929190611c27565b60405180910390f35b60006102bd61156d565b606060006001866040518082805190602001908083835b6020831015156102f957805182526020820191506020810190506020830392506102d4565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206040805190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820180548060200260200160405190810160405280929190818152602001828054801561041257602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190600101908083116103c8575b50505050508152505092508473ffffffffffffffffffffffffffffffffffffffff16836000015173ffffffffffffffffffffffffffffffffffffffff16141561045e57600193506104d9565b82602001519150600090505b81518110156104d4578473ffffffffffffffffffffffffffffffffffffffff16828281518110151561049857fe5b9060200190602002015173ffffffffffffffffffffffffffffffffffffffff1614156104c757600193506104d9565b808060010191505061046a565b600093505b50505092915050565b600060606104ee61159d565b600092505b87518310156107a457878381518110151561050a57fe5b9060200190602002015191506000826040518082805190602001908083835b60208310151561054e5780518252602082019150602081019050602083039250610529565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681525050905080604001516000141561079757868381518110151561061e57fe5b90602001906020020151816000018181525050858381518110151561063f57fe5b90602001906020020151816020018181525050848381518110151561066057fe5b90602001906020020151816040018181525050838381518110151561068157fe5b90602001906020020151816060019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff1681525050806000836040518082805190602001908083835b6020831015156106fb57805182526020820191506020810190506020830392506106d6565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055509050505b82806001019350506104f3565b5050505050505050565b60006107b9836113cc565b15156107ca5762018a9890506108bb565b6107d483836102b3565b156107e45762018a9990506108bb565b6001836040518082805190602001908083835b60208310151561081c57805182526020820191506020810190506020830392506107f7565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206001018290806001815401808255809150509060018203906000526020600020016000909192909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050600090505b92915050565b60006108cd82326102b3565b9050919050565b60006108de61159d565b6108e885326102b3565b15156108f75760009150610b1d565b6000856040518082805190602001908083835b60208310151561092f578051825260208201915060208101905060208303925061090a565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152505090506001816000015101816000018181525050438160200181815250504281604001818152505032816060019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff1681525050806000866040518082805190602001908083835b602083101515610a7a5780518252602082019150602081019050602083039250610a55565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550905050806000015191505b509392505050565b600080600080610b34866113cc565b1515610b455762018a989350610f97565b610b4f86866102b3565b1515610b605762018a9a9350610f97565b6001866040518082805190602001908083835b602083101515610b985780518252602082019150602081019050602083039250610b73565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020600101805490509250600091505b82821015610ccc578473ffffffffffffffffffffffffffffffffffffffff166001876040518082805190602001908083835b602083101515610c305780518252602082019150602081019050602083039250610c0b565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060010183815481101515610c7357fe5b9060005260206000200160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415610cbf57610ccc565b8180600101925050610bd9565b60018303821415610d87576001866040518082805190602001908083835b602083101515610d0f5780518252602082019150602081019050602083039250610cea565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060010160018403815481101515610d5557fe5b9060005260206000200160006101000a81549073ffffffffffffffffffffffffffffffffffffffff0219169055610f10565b8190505b60018303811015610f0f576001866040518082805190602001908083835b602083101515610dce5780518252602082019150602081019050602083039250610da9565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060010160018201815481101515610e1457fe5b9060005260206000200160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff166001876040518082805190602001908083835b602083101515610e775780518252602082019150602081019050602083039250610e52565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060010182815481101515610eba57fe5b9060005260206000200160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508080600101915050610d8b565b5b6001866040518082805190602001908083835b602083101515610f485780518252602082019150602081019050602083039250610f23565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020600101805480919060019003610f9191906115dc565b50600093505b50505092915050565b600080600080610fae61159d565b6000866040518082805190602001908083835b602083101515610fe65780518252602082019150602081019050602083039250610fc1565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020608060405190810160405290816000820154815260200160018201548152602001600282015481526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681525050905080600001519450806020015193508060400151925080606001519150509193509193565b600080808390806001815401808255809150509060018203906000526020600020016000909192909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505060408051908101604052808473ffffffffffffffffffffffffffffffffffffffff168152602001828054806020026020016040519081016040528092919081815260200182805480156111cf57602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019060010190808311611185575b50505050508152506001856040518082805190602001908083835b60208310151561120f57805182526020820191506020810190506020830392506111ea565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010190805190602001906112a5929190611608565b50905050600191505092915050565b60006060600091506112c5836113cc565b15156112d25762018a9891505b6001836040518082805190602001908083835b60208310151561130a57805182526020820191506020810190506020830392506112e5565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206001018054806020026020016040519081016040528092919081815260200182805480156113c057602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019060010190808311611376575b50505050509050915091565b60006113d661156d565b6001836040518082805190602001908083835b60208310151561140e57805182526020820191506020810190506020830392506113e9565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206040805190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820180548060200260200160405190810160405280929190818152602001828054801561152757602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190600101908083116114dd575b50505050508152505090503273ffffffffffffffffffffffffffffffffffffffff16816000015173ffffffffffffffffffffffffffffffffffffffff1614915050919050565b6040805190810160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001606081525090565b608060405190810160405280600081526020016000815260200160008152602001600073ffffffffffffffffffffffffffffffffffffffff1681525090565b815481835581811115611603578183600052602060002091820191016116029190611692565b5b505050565b828054828255906000526020600020908101928215611681579160200282015b828111156116805782518260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555091602001919060010190611628565b5b50905061168e91906116b7565b5090565b6116b491905b808211156116b0576000816000905550600101611698565b5090565b90565b6116f791905b808211156116f357600081816101000a81549073ffffffffffffffffffffffffffffffffffffffff0219169055506001016116bd565b5090565b90565b60006117068235611df4565b905092915050565b600082601f830112151561172157600080fd5b813561173461172f82611cc9565b611c9c565b9150818183526020840193506020810190508385602084028201111561175957600080fd5b60005b83811015611789578161176f88826116fa565b84526020840193506020830192505060018101905061175c565b5050505092915050565b600082601f83011215156117a657600080fd5b81356117b96117b482611cf1565b611c9c565b9150818183526020840193506020810190508360005b838110156117ff57813586016117e5888261188e565b8452602084019350602083019250506001810190506117cf565b5050505092915050565b600082601f830112151561181c57600080fd5b813561182f61182a82611d19565b611c9c565b9150818183526020840193506020810190508385602084028201111561185457600080fd5b60005b83811015611884578161186a888261193a565b845260208401935060208301925050600181019050611857565b5050505092915050565b600082601f83011215156118a157600080fd5b81356118b46118af82611d41565b611c9c565b915080825260208301602083018583830111156118d057600080fd5b6118db838284611e1e565b50505092915050565b600082601f83011215156118f757600080fd5b813561190a61190582611d6d565b611c9c565b9150808252602083016020830185838301111561192657600080fd5b611931838284611e1e565b50505092915050565b60006119468235611e14565b905092915050565b600080600080600060a0868803121561196657600080fd5b600086013567ffffffffffffffff81111561198057600080fd5b61198c88828901611793565b955050602086013567ffffffffffffffff8111156119a957600080fd5b6119b588828901611809565b945050604086013567ffffffffffffffff8111156119d257600080fd5b6119de88828901611809565b935050606086013567ffffffffffffffff8111156119fb57600080fd5b611a0788828901611809565b925050608086013567ffffffffffffffff811115611a2457600080fd5b611a308882890161170e565b9150509295509295909350565b600060208284031215611a4f57600080fd5b600082013567ffffffffffffffff811115611a6957600080fd5b611a75848285016118e4565b91505092915050565b60008060408385031215611a9157600080fd5b600083013567ffffffffffffffff811115611aab57600080fd5b611ab7858286016118e4565b9250506020611ac8858286016116fa565b9150509250929050565b600080600060608486031215611ae757600080fd5b600084013567ffffffffffffffff811115611b0157600080fd5b611b0d868287016118e4565b935050602084013567ffffffffffffffff811115611b2a57600080fd5b611b36868287016118e4565b925050604084013567ffffffffffffffff811115611b5357600080fd5b611b5f868287016118e4565b9150509250925092565b611b7281611dbe565b82525050565b6000611b8382611da6565b808452602084019350611b9583611d99565b60005b82811015611bc757611bab868351611b69565b611bb482611db1565b9150602086019550600181019050611b98565b50849250505092915050565b611bdc81611dde565b82525050565b611beb81611dea565b82525050565b6000602082019050611c066000830184611bd3565b92915050565b6000602082019050611c216000830184611be2565b92915050565b6000604082019050611c3c6000830185611be2565b8181036020830152611c4e8184611b78565b90509392505050565b6000608082019050611c6c6000830187611be2565b611c796020830186611be2565b611c866040830185611be2565b611c936060830184611b69565b95945050505050565b6000604051905081810181811067ffffffffffffffff82111715611cbf57600080fd5b8060405250919050565b600067ffffffffffffffff821115611ce057600080fd5b602082029050602081019050919050565b600067ffffffffffffffff821115611d0857600080fd5b602082029050602081019050919050565b600067ffffffffffffffff821115611d3057600080fd5b602082029050602081019050919050565b600067ffffffffffffffff821115611d5857600080fd5b601f19601f8301169050602081019050919050565b600067ffffffffffffffff821115611d8457600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b600081519050919050565b6000602082019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60008115159050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b828183376000838301525050505600a265627a7a7230582098195aa3a9ab415b42025dbfb376ffc8cebc8d5f3480498e64860867992a1edd6c6578706572696d656e74616cf50037";

    public static final String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"operatorAddress\",\"type\":\"address\"}],\"name\":\"checkOperatorExist\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string[]\"},{\"name\":\"lastSequence\",\"type\":\"uint256[]\"},{\"name\":\"lastBlock\",\"type\":\"uint256[]\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256[]\"},{\"name\":\"lastSender\",\"type\":\"address[]\"}],\"name\":\"flushSnapshot\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"operatorAddress\",\"type\":\"address\"}],\"name\":\"addOperator\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"checkOperatorPermission\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"eventContent\",\"type\":\"string\"},{\"name\":\"extensions\",\"type\":\"string\"}],\"name\":\"publishWeEvent\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"operatorAddress\",\"type\":\"address\"}],\"name\":\"delOperator\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"getSnapshot\",\"outputs\":[{\"name\":\"lastSequence\",\"type\":\"uint256\"},{\"name\":\"lastBlock\",\"type\":\"uint256\"},{\"name\":\"lastTimestamp\",\"type\":\"uint256\"},{\"name\":\"lastSender\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"},{\"name\":\"ownerAddress\",\"type\":\"address\"}],\"name\":\"addTopicACL\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"topicName\",\"type\":\"string\"}],\"name\":\"listOperator\",\"outputs\":[{\"name\":\"code\",\"type\":\"uint256\"},{\"name\":\"operatorArray\",\"type\":\"address[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

    public static final TransactionDecoder transactionDecoder = new TransactionDecoder(ABI, BINARY);

    public static final String FUNC_CHECKOPERATOREXIST = "checkOperatorExist";

    public static final String FUNC_FLUSHSNAPSHOT = "flushSnapshot";

    public static final String FUNC_ADDOPERATOR = "addOperator";

    public static final String FUNC_CHECKOPERATORPERMISSION = "checkOperatorPermission";

    public static final String FUNC_PUBLISHWEEVENT = "publishWeEvent";

    public static final String FUNC_DELOPERATOR = "delOperator";

    public static final String FUNC_GETSNAPSHOT = "getSnapshot";

    public static final String FUNC_ADDTOPICACL = "addTopicACL";

    public static final String FUNC_LISTOPERATOR = "listOperator";

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

    public static TransactionDecoder getTransactionDecoder() {
        return transactionDecoder;
    }

    public RemoteCall<Boolean> checkOperatorExist(String topicName, String operatorAddress) {
        final Function function = new Function(FUNC_CHECKOPERATOREXIST,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
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

    public RemoteCall<TransactionReceipt> addOperator(String topicName, String operatorAddress) {
        final Function function = new Function(
                FUNC_ADDOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void addOperator(String topicName, String operatorAddress, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_ADDOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String addOperatorSeq(String topicName, String operatorAddress) {
        final Function function = new Function(
                FUNC_ADDOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple2<String, String> getAddOperatorInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDOPERATOR,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Address>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple2<String, String>(

                (String) results.get(0).getValue(),
                (String) results.get(1).getValue()
        );
    }

    public Tuple1<BigInteger> getAddOperatorOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_ADDOPERATOR,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public RemoteCall<Boolean> checkOperatorPermission(String topicName) {
        final Function function = new Function(FUNC_CHECKOPERATORPERMISSION,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
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

    public RemoteCall<TransactionReceipt> delOperator(String topicName, String operatorAddress) {
        final Function function = new Function(
                FUNC_DELOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void delOperator(String topicName, String operatorAddress, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_DELOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String delOperatorSeq(String topicName, String operatorAddress) {
        final Function function = new Function(
                FUNC_DELOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(operatorAddress)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple2<String, String> getDelOperatorInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_DELOPERATOR,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Address>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple2<String, String>(

                (String) results.get(0).getValue(),
                (String) results.get(1).getValue()
        );
    }

    public Tuple1<BigInteger> getDelOperatorOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_DELOPERATOR,
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

    public RemoteCall<TransactionReceipt> addTopicACL(String topicName, String ownerAddress) {
        final Function function = new Function(
                FUNC_ADDTOPICACL,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(ownerAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void addTopicACL(String topicName, String ownerAddress, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_ADDTOPICACL,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(ownerAddress)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String addTopicACLSeq(String topicName, String ownerAddress) {
        final Function function = new Function(
                FUNC_ADDTOPICACL,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                        new org.fisco.bcos.web3j.abi.datatypes.Address(ownerAddress)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple2<String, String> getAddTopicACLInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDTOPICACL,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Address>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple2<String, String>(

                (String) results.get(0).getValue(),
                (String) results.get(1).getValue()
        );
    }

    public Tuple1<Boolean> getAddTopicACLOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_ADDTOPICACL,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        ;
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
        );
    }

    public RemoteCall<Tuple2<BigInteger, List<String>>> listOperator(String topicName) {
        final Function function = new Function(FUNC_LISTOPERATOR,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }, new TypeReference<DynamicArray<Address>>() {
                }));
        return new RemoteCall<Tuple2<BigInteger, List<String>>>(
                new Callable<Tuple2<BigInteger, List<String>>>() {
                    @Override
                    public Tuple2<BigInteger, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, List<String>>(
                                (BigInteger) results.get(0).getValue(),
                                convertToNative((List<Address>) results.get(1).getValue()));
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
