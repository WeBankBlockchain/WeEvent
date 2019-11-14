package com.webank.weevent.governance.code;

public enum ErrorCode {

    SUCCESS(0, "success"),
    BROKER_CONNECT_ERROR(100100, "broker connect error!"),
    WEBASE_CONNECT_ERROR(100101, "webase connect error!"),
    SEND_EMAIL_ERROR(100102, "send email failed"),
    ACCESS_DENIED(100103, "access denied"),
    BUILD_URL_METHOD(100104, "build url method fail"),
    ILLEGAL_INPUT(100105, "illegal input"),
    WEBASE_REQUIRED(100105, "when it is version 1.3, WeBase url is required"),
    PROCESS_CONNECT_ERROR(100106, "process connect error!"),
    NO_MAILBOX_CONFIGURED(100107, "no mailbox configured"),
    BROKER_REPEAT(100108, "the brokerUrl already has a record in the database"),
    TOPIC_EXISTS(100109, "topic already exists"),
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
