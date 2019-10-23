package com.webank.weevent.processor.utils;

public enum StatusCode {
    SUCCESS(0, "success"),
    //client error(100000, 200000)
    TOPIC_ALREADY_EXIST(100100, "topic already exist"),

    TOPIC_NOT_EXIST(100101, "topic not exist"),

    TOPIC_EXCEED_MAX_LENGTH(100102, "topic name exceeds max length[64 bytes]"),

    TOPIC_IS_BLANK(100103, "topic name is blank"),

    TOPIC_PAGE_INDEX_INVALID(100104, "page index should be an integer start from 0"),

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
    StatusCode(int code, String codeDesc) {
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
