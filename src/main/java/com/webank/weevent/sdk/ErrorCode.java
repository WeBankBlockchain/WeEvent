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

    TOPIC_EXCEED_MAX_LENGTH(100102, "topic name exceeds max length[32 bytes]"),

    TOPIC_IS_BLANK(100103, "topic name is blank"),

    TOPIC_PAGE_INDEX_INVALID(100104, "page index should be an integer start from 0"),

    TOPIC_MODEL_MAP_IS_NULL(100105, "topic model map is empty or has empty value"),

    TOPIC_CONTAIN_INVALID_CHAR(100106, "topic name contain invalid char, Ascii must be in[32, 128]"),

    TOPIC_PAGE_SIZE_INVALID(100107, "page size should be an integer in(1, 100)"),

    TOPIC_NOT_MATCH(100108, "topic name not match with last"),

    EVENT_CONTENT_IS_BLANK(100200, "event content is blank"),

    EVENT_CONTENT_EXCEEDS_MAX_LENGTH(100201, "event content exceeds max length[10k bytes]"),

    SEND_CALL_BACK_IS_NULL(100202, "send call back instance is null"),

    EVENT_CONTENT_CHARSET(100203, "event content must be utf-8"),

    EVENT_ID_IS_BLANK(100300, "eventId is blank"),

    EVENT_ID_EXCEEDS_MAX_LENGTH(100301, "eventId exceeds max length[32 bytes]"),

    EVENT_ID_IS_ILLEGAL(100302, "eventId is illegal"),

    EVENT_ID_NOT_EXIST(100303, "eventId is not exist"),

    EVENT_ID_IS_MISMATCH(100304, "eventId is mismatch with block chain"),

    OFFSET_IS_BLANK(100500, "subscribe interface offset param is blank"),

    URL_INVALID_FORMAT(100501, "invalid url format"),

    URL_CONNECT_FAILED(100502, "failed to connect url"),

    SUBSCRIPTIONID_IS_BLANK(100503, "subscribe interface subscription id is blank"),

    SUBSCRIPTIONID_NOT_EXIST(100504, "subscribe id is not exist"),

    SUBSCRIPTIONID_FORMAT_INVALID(100505, "subscribe id format invalid"),

    MQTT_NO_BROKER_URL(100600, "no mqtt.broker.url configuration, can't support mqtt"),

    CGI_SUBSCRIPTION_NO_ZOOKEEPER(100601, "no broker.zookeeper.ip configuration, can't support CGI subscription"),

    HA_ROUTE_TO_MASTER_FAILED(100602, "route request to master failed"),

    SDK_TLS_INIT_FAILED(101001, "init tsl ca failed"),

    SDK_JMS_INIT_FAILED(101002, "init jms connection factory failed"),

    SDK_JMS_EXCEPTION_STOMP_EXECUTE(101003, "stomp command execute failed"),

    SDK_JMS_EXCEPTION_STOMP_TIMEOUT(101004, "stomp command invoke timeout"),

    SDK_JMS_EXCEPTION_JSON_ENCODE(101005, "encode WeEvent to json failed"),

    SDK_JMS_EXCEPTION_JSON_DECODE(101006, "decode WeEvent from json failed"),

    SDK_JMS_EXCEPTION(101010, "jms exception"),

    //server error(200000, 300000)
    TOPIC_CONTROLLER_IS_NULL(200100, "init failed, see fisco.topic-controller.contract-address in properties"),

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

    WE3SDK_INIT_ERROR(200209, "init web3sdk failed"),
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
