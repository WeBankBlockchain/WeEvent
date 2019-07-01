package com.webank.weevent.governance.code;

public enum ErrorCode {

    SUCCESS(0, "success"), 
    BROKER_CONNECT_ERROR(100100, "broker connect error!"), 
    WEBASE_CONNECT_ERROR(100101, "webase connect error!"),
    SEND_EMAIL_ERROR(100102,"send email failed"),
    ACCESS_DENIED(100103,"access denied"),
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
     * @param code
     *            The ErrorCode
     * @param codeDesc
     *            The ErrorCode Description
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
