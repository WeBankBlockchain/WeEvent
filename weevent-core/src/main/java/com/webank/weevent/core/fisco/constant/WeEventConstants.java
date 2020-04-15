package com.webank.weevent.core.fisco.constant;

import java.math.BigInteger;

public class WeEventConstants {
    /**
     * The Constant GAS_PRICE.
     */
    public static final BigInteger GAS_PRICE = new BigInteger("99999999999");

    /**
     * The Constant Address Empty.
     */
    public static final String ADDRESS_EMPTY = "0x0000000000000000000000000000000000000000";

    /**
     * The Constant GAS_LIMIT.
     */
    public static final BigInteger GAS_LIMIT = new BigInteger("9999999999999");

    /**
     * The Constant INITIAL_VALUE.
     */
    public static final BigInteger INITIAL_VALUE = new BigInteger("0");

    /**
     * Max length for topic name.
     */
    public static final Integer TOPIC_NAME_MAX_LENGTH = 64;

    /**
     * topic name encode length.
     */
    public static final Integer TOPIC_NAME_ENCODE_LENGTH = 8;

    /**
     * hash length.
     */
    public static final Integer HASH_LENGTH = 32;

    /**
     * Max length for event id.
     */
    public static final Integer EVENT_ID_MAX_LENGTH = 64;

    /**
     * Max length for event content.
     */
    public static final Integer EVENT_CONTENT_MAX_LENGTH = 10240;

    /**
     * Max length for event extensions.
     */
    public static final Integer EVENT_EXTENSIONS_MAX_LENGTH = 1024;
    /**
     * Event ID split char.
     */
    public static final String EVENT_ID_SPLIT_CHAR = "-";

    /**
     * Extensions prefix char.
     */
    public static final String EXTENSIONS_PREFIX_CHAR = "weevent-";

    /**
     * Extensions will message.
     */
    public static final String EXTENSIONS_WILL_MESSAGE = "weevent-willmessage";

    /**
     * event topic.
     */
    public static final String EVENT_TOPIC = "topic";

    /**
     * event id.
     */
    public static final String EXTENSIONS_EVENT_ID = "eventId";

    /**
     * event topic.
     */
    public static final String EVENT_GROUP_ID = "groupId";

    /**
     * event content.
     */
    public static final String EVENT_CONTENT = "content";

    /**
     * check the API type,this is use for jsonrpc
     */
    public static final String JSONRPCTYPE = "jsonrpc";

    /**
     * check the API type,this is use for restful
     */
    public static final String RESTFULTYPE = "restful";

    /**
     * check the API type,this is use for mqtt
     */
    public static final String MQTTTYPE = "mqtt";

    /**
     * check the API type,this is use for mqtt
     */
    public static final String STOMPTYPE = "stomp";
    
    /**
     * The prefix of FISCO-BCOS version 2.X
     */
    public static final String FISCO_BCOS_2_X_VERSION_PREFIX = "2.";

    public static final String FISCO = "fisco";

    public static final String FABRIC = "fabric";

    public static final String TOPIC_ALREADY_EXIST = "100100";

    public static final String NODE_TYPE_SEALER = "sealer";

    public static final String NODE_TYPE_OBSERVER = "observer";

    public static final int DEFAULT_CHAINCODE_PARAM_COUNT = 4;

    /**
     * The transaction poll attempts (max).
     */
    public static final Integer POLL_TRANSACTION_ATTEMPTS = 10;

    /**
     * The FISCO-BCOS Address pattern.
     */
    public static final String FISCO_BCOS_ADDRESS_PATTERN = "0x[a-fA-f0-9]{40}";

    /**
     * The sign data pattern.
     */
    public static final String SIGN_DATA_PATTERN = "0x([a-fA-f0-9]*)";

}
