package com.webank.weevent.sdk;

/**
 * client error code in (100000, 200000)
 * server error code in (200000, 300000)
 */
public enum ErrorCode {

    SUCCESS(0, "success"),

    //client error(100000, 200000)
    TOPIC_ALREADY_EXIST(100100, "topic already exist"),

    TOPIC_NOT_EXIST(100101, "topic not exist"),

    TOPIC_EXCEED_MAX_LENGTH(100102, "topic name exceeds max length[64 bytes]"),

    TOPIC_IS_BLANK(100103, "topic name is blank"),

    TOPIC_PAGE_INDEX_INVALID(100104, "page index should be an integer start from 0"),

    TOPIC_LIST_IS_NULL(100105, "topic list is empty or has empty value"),

    TOPIC_CONTAIN_INVALID_CHAR(100106, "topic name contain invalid char, ascii must be in[32, 128] except wildcard(+,#)"),

    TOPIC_PAGE_SIZE_INVALID(100107, "page size should be an integer in(1, 100)"),

    TOPIC_NOT_MATCH(100108, "topic name not match with last subscribe"),

    PATTERN_INVALID(100109, "invalid topic pattern"),

    TOPIC_TAG_NOT_MATCH(100110, "topic tag not match with last subscribe"),

    EVENT_CONTENT_IS_BLANK(100200, "event content is blank"),

    EVENT_CONTENT_EXCEEDS_MAX_LENGTH(100201, "event content exceeds max length[10k bytes]"),

    SEND_CALL_BACK_IS_NULL(100202, "send call back instance is null"),

    EVENT_CONTENT_CHARSET(100203, "event content must be utf-8"),

    EVENT_EXTENSIONS_EXCEEDS_MAX_LENGTH(100204, "event extensions exceeds max length[1k bytes]"),

    EVENT_EXTENSIONS_IS_NUll(100205, "event extensions is null"),

    EVENT_EXTENSIONS_KEY_INVALID(100206, "event extensions key not startwith weevent-"),

    EVENT_ID_IS_BLANK(100300, "eventId is blank"),

    EVENT_ID_EXCEEDS_MAX_LENGTH(100301, "eventId exceeds max length[64 bytes]"),

    EVENT_ID_IS_ILLEGAL(100302, "eventId is illegal"),

    EVENT_ID_NOT_EXIST(100303, "eventId is not exist"),

    EVENT_ID_IS_MISMATCH(100304, "eventId is mismatch with block chain"),

    EVENT_GROUP_ID_NOT_FOUND(100305, "event group id is not found"),

    EVENT_GROUP_ID_INVALID(100306, "event group id should be a string start from 1"),

    OFFSET_IS_BLANK(100500, "subscribe interface offset param is blank"),

    URL_INVALID_FORMAT(100501, "invalid url format"),

    URL_CONNECT_FAILED(100502, "failed to connect url"),

    SUBSCRIPTIONID_IS_BLANK(100503, "subscribe interface subscription id is blank"),

    SUBSCRIPTIONID_NOT_EXIST(100504, "subscribe id is not exist"),

    SUBSCRIPTIONID_FORMAT_INVALID(100505, "subscribe id format invalid"),

    TOPIC_TAG_IS_BLANK(100506, "topic's tag is blank"),

    SUBSCRIPTIONID_ALREADY_EXIST(100507, "subscriptionId is already exist, can not subscribe again"),

    MQTT_NO_BROKER_URL(100600, "no mqtt.broker.url configuration, can't support mqtt"),

    CGI_SUBSCRIPTION_NO_ZOOKEEPER(100601, "no broker.zookeeper.ip configuration, can't support CGI subscription"),

    HA_ROUTE_TO_MASTER_FAILED(100602, "route request to master failed"),

    SDK_TLS_INIT_FAILED(101001, "init tsl ca failed"),

    SDK_JMS_INIT_FAILED(101002, "init jms connection factory failed"),

    SDK_JMS_EXCEPTION_STOMP_EXECUTE(101003, "stomp command execute failed"),

    SDK_JMS_EXCEPTION_STOMP_TIMEOUT(101004, "stomp command invoke timeout"),

    SDK_JMS_EXCEPTION(101010, "jms exception"),

    USER_PASSWORD_ISBLANK(101011, "stomp password is blank"),

    USER_NAME_ISBLANK(101012, "stomp user name is blank"),

    PARAM_ISBLANK(101013, "the input param is blank"),

    PARAM_ISEMPTY(101014, "the input param is empty"),

    PARAM_ISNULL(101014, "the input param is null"),

    PARAM_IS_NOT_MAP(101015, "the input param class is not Map"),

    //server error(200000, 300000)
    TOPIC_CONTROLLER_IS_NULL(200100, "get topic control address from CNS failed, please deploy it first"),

    CONSUMER_ALREADY_STARTED(200102, "consumer already started"),

    PRODUCER_SEND_CALLBACK_IS_NULL(200103, "producer send callback is null"),

    CONSUMER_LISTENER_IS_NULL(200104, "consumer listener is null"),

    TRANSACTION_TIMEOUT(200201, "the transaction is timeout."),

    TRANSACTION_EXECUTE_ERROR(200202, "the transaction does not correctly executed."),

    UNKNOWN_ERROR(200203, "unknown error, please check error log."),

    EVENT_COMPRESS_ERROR(200204, "event compress error"),

    GET_BLOCK_HEIGHT_ERROR(200205, "get block height failed due to InterruptedException|ExecutionException|TimeoutException|RuntimeException"),

    TRANSACTION_RECEIPT_IS_NULL(200206, "transaction reception is null"),

    DEPLOY_CONTRACT_ERROR(200207, "deploy contract failed"),

    LOAD_CONTRACT_ERROR(200208, "load contract failed"),

    WEB3SDK_INIT_ERROR(200209, "init web3sdk failed"),

    WEB3SDK_VERSION_NOT_SUPPORT(200210, "FISCO-BCOS 1.x, do not support group"),

    WEB3SDK_UNKNOWN_GROUP(200211, "FISCO-BCOS 2.x, unknown group id"),

    SUBSCRIPTION_NOTIFY_QUEUE_FULL(200212, "onEvent is blocked too long, notify queue is full"),

    UNKNOWN_SOLIDITY_VERSION(200213, "unknown contract version"),

    WEB3SDK_RPC_ERROR(200214, "we3sdk's rpc failed"),

    JSON_ENCODE_EXCEPTION(200215, "encode Object to json failed"),

    JSON_DECODE_EXCEPTION(200216, "decode Object from json failed"),

    FABRICSDK_CHANNEL_NAME_INVALID(200300, "Fabric 1.4x, channel name invalid"),

    FABRICSDK_GETBLOCKINFO_ERROR(200301, "Fabric 1.4x, get blockInfo error"),

    FABRICSDK_CHAINCODE_INVOKE_FAILED(200302, "Fabric 1.4x, execute chaincode invoke failed"),
    ;

    /**
     * error code
     */
    private int code;

    /**
     * error message
     */
    private String codeDesc;

    /**
     * Error Code Constructor.
     *
     * @param code The ErrorCode
     * @param codeDesc The ErrorCode Description
     */
    ErrorCode(int code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    /**
     * Get the Error Code.
     *
     * @return the ErrorCode
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the ErrorCode Description.
     *
     * @return the ErrorCode Description
     */
    public String getCodeDesc() {
        return codeDesc;
    }
}
