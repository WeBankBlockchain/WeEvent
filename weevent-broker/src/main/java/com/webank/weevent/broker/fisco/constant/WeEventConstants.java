package com.webank.weevent.broker.fisco.constant;

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
     * The Constant CallContract Timeout.
     */
    public static final Integer TIME_OUT = 102;

    /**
     * The Constant GAS_LIMIT.
     */
    public static final BigInteger GAS_LIMIT = new BigInteger("9999999999999");

    /**
     * The Constant for default deploy contracts timeout.
     */
    public static final Integer DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS = 15;

    /**
     * The Constant INIIIAL_VALUE.
     */
    public static final BigInteger INILITIAL_VALUE = new BigInteger("0");

    /**
     * The Constant default timeout for getting transaction.
     */
    public static final Integer TRANSACTION_RECEIPT_TIMEOUT = 13;

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
     * Extensions default groupid.
     */
    public static final String DEFAULT_GROUP_ID = "1";

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
     * wildcard character for one layer.
     * see MQTT specification.
     */
    public static final String WILD_CARD_ONE_LAYER = "+";

    /**
     * wildcard for one layer.
     * see MQTT specification.
     */
    public static final String WILD_CARD_ALL_LAYER = "#";

    /**
     * character to split layer.
     * see MQTT specification.
     */
    public static final String LAYER_SEPARATE = "/";

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

}
