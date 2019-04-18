package com.webank.weevent.broker.fisco.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bouncycastle.util.encoders.Hex;

/**
 * Data type conversion utilities between solidity data type and java data type.
 */
@Slf4j
public final class DataTypeUtils {
    /**
     *  encode eventId
     *
     * @param topicName
     * @param eventBlockNumber blockchain blocknumber
     * @param eventSeq eventSeq number
     * @return encodeString
     */
    public static String encodeEventId(String topicName,int eventBlockNumber,int eventSeq){
        StringBuilder sb = new StringBuilder();
        sb.append(genTopicNameHash(topicName));
        sb.append(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        sb.append(eventSeq);
        sb.append(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        sb.append(eventBlockNumber);
        return sb.toString();
    }

    /**
     *  generate topicName hash
     *
     * @param topicName
     * @return substring left 4bit hash data to hex encode
     */
    public static String genTopicNameHash(String topicName){
        String encodeData = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(topicName.getBytes());
            encodeData = new String(Hex.encode(messageDigest)).substring(0, WeEventConstants.TOPIC_NAME_ENCODE_LENGTH);
        }catch (NoSuchAlgorithmException e){
            log.error("NoSuchAlgorithmException:{}",e.getMessage());
        }
        return encodeData;
    }

    /**
     *  decode eventId get seq
     *
     * @param eventId
     * @return seq
     */
    public static Long decodeSeq(String eventId) throws BrokerException{
        String[] tokens = eventId.split(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        if (tokens.length != 3) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }

        if (tokens[0].length() != WeEventConstants.TOPIC_NAME_ENCODE_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        return DataTypeUtils.String2Long(tokens[1]);
    }

    /**
     *  decode eventId get blockNumber
     *
     * @param eventId
     * @return blockNumber
     */
    public static Long decodeBlockNumber(String eventId) throws BrokerException{
        String[] tokens = eventId.split(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        if (tokens.length != 3) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        if (tokens[0].length() != WeEventConstants.TOPIC_NAME_ENCODE_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        return DataTypeUtils.String2Long(tokens[2]);
    }

    /**
     *  decode eventId get topicName hash
     *
     * @param eventId
     * @return topicName hash
     */
    public static String decodeTopicNameHash(String eventId) throws BrokerException{
        String[] tokens = eventId.split(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        if (tokens.length != 3) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        if (tokens[0].length() != WeEventConstants.TOPIC_NAME_ENCODE_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        return tokens[0];
    }
    /**
     * String to bytes 32.
     *
     * @param string the string
     * @return the bytes 32
     */
    public static Bytes32 stringToBytes32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return new Bytes32(byteValueLen32);
    }

    /**
     * Uint 256 to int.
     *
     * @param value the value
     * @return the int
     */
    public static int uint256ToInt(Uint256 value) {
        return value.getValue().intValue();
    }


    /**
     * Convert a Byte32 data to Java String. IMPORTANT NOTE: Byte to String is not 1:1 mapped. So -
     * Know your data BEFORE do the actual transform! For example, Deximal Bytes, or ASCII Bytes are
     * OK to be in Java String, but Encrypted Data, or raw Signature, are NOT OK.
     *
     * @param bytes32 the bytes 32
     * @return String
     */
    public static String bytes32ToString(Bytes32 bytes32) {
        byte[] strs = bytes32.getValue();
        String str = new String(strs);
        return str.trim();
    }

    /**
     * Long to int 256.
     *
     * @param value the value
     * @return the int 256
     */
    public static Uint256 longToUint256(long value) {
        return new Uint256(value);
    }

    /**
     * Int to Uint 256.
     *
     * @param value the value
     * @return the Uint 256
     */
    public static Uint256 intToUint256(int value) {
        return new Uint256(value);
    }

    /**
     * Bytes 32 dynamic array to string array
     *
     * @param bytes32DynamicArray the bytes 32 dynamic array
     * @return the string[]
     */
    public static String[] bytes32DynamicArrayToStringArrayWithoutTrim(
            DynamicArray<Bytes32> bytes32DynamicArray) {
        List<Bytes32> bytes32List = bytes32DynamicArray.getValue();
        String[] stringArray = new String[bytes32List.size()];
        for (int i = 0; i < bytes32List.size(); i++) {
            stringArray[i] = bytes32ToString(bytes32List.get(i));
        }
        return stringArray;
    }

    /**
     * String2Long, return 0L if exception.
     *
     * @param value the value
     * @return java.lang.Long
     */
    public static Long String2Long(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
