package com.webank.weevent.broker.fisco.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.dto.ContractContext;
import com.webank.weevent.broker.fisco.web3sdk.v2.Web3SDK2Wrapper;
import com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionEncoder;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.Numeric;

public class RawTransactionUtils {

    public static String buildWeEvent(WeEvent event) throws BrokerException {
        final Function function = new Function(
                Topic.FUNC_PUBLISHWEEVENT,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(event.getTopic()),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(new String(event.getContent(), StandardCharsets.UTF_8)),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(DataTypeUtils.object2Json(event.getExtensions()))),
                Collections.<TypeReference<?>>emptyList());
        return FunctionEncoder.encode(function);
    }

    public static String buildACLData(String topicName, String functionName, String address) {
        final Function function;
        if (StringUtils.isBlank(address)) {
            function = new Function(
                    functionName,
                    Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName)),
                    Collections.<TypeReference<?>>emptyList());
        } else {
            function = new Function(
                    functionName,
                    Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(topicName),
                            new org.fisco.bcos.web3j.abi.datatypes.Address(address)),
                    Collections.<TypeReference<?>>emptyList());
        }
        return FunctionEncoder.encode(function);
    }

    public static ExtendedRawTransaction getRawTransaction(String groupId, String data, ContractContext contractContext) {
        Random r = new SecureRandom();
        BigInteger randomid = new BigInteger(250, r);
        ExtendedRawTransaction rawTransaction =
                ExtendedRawTransaction.createTransaction(
                        randomid,
                        BigInteger.valueOf(contractContext.getGasPrice()),
                        BigInteger.valueOf(contractContext.getGasLimit()),
                        BigInteger.valueOf(contractContext.getBlockLimit()),
                        contractContext.getTopicAddress(),
                        BigInteger.ZERO,
                        data,
                        new BigInteger(contractContext.getChainId()),
                        new BigInteger(groupId),
                        null);
        return rawTransaction;
    }

    public static Credentials getFixedAccountCredentials() {
        FiscoConfig fiscoConfig = new FiscoConfig();
        fiscoConfig.load();
        return Web3SDK2Wrapper.getCredentials(fiscoConfig);
    }

    public static Credentials getExternalAccountCredentials() {
        return GenCredential.create();
    }

    public static String signData(ExtendedRawTransaction rawTransaction, Credentials credentials) {
        byte[] signedMessage = new byte[0];
        try {
            signedMessage = ExtendedTransactionEncoder.signMessage(rawTransaction, credentials);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Numeric.toHexString(signedMessage);
    }
}
