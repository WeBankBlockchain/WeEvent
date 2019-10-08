package com.webank.weevent.governance.exception;

import com.webank.weevent.governance.code.ErrorCode;

public class GovernanceException extends Exception {

    private final static String WIKIUrl = "https://weeventdoc.readthedocs.io/zh_CN/latest/faq/weevent.html";

    /**
     * Error code.
     */
    protected int code;

    /**
     * Error message.
     */
    protected String message;

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * getText
     *
     * @param code
     *            the code
     * @param message
     *            the message
     * @return java.lang.String
     */
    private static String getText(int code, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Code: ");
        sb.append(code);
        sb.append(", Message: ");
        sb.append(message);
        sb.append("\nFor more information, please visit wiki: ");
        sb.append(WIKIUrl);
        return sb.toString();
    }

    /**
     * Construction.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public GovernanceException(String message, Throwable cause) {
        super(getText(-1, message+","+cause.getMessage()),cause);
        this.code = -1;
        this.message = message+","+cause.getMessage();
    }

    /**
     * Construction.
     *
     * @param message
     *            the message
     */
    public GovernanceException(String message) {
        super(getText(-1, message));
        this.code = -1;
        this.message = message;
    }

    /**
     * Construction.
     *
     * @param errorCode
     *            the code and message
     */
    public GovernanceException(ErrorCode errorCode) {
        super(getText(errorCode.getCode(), errorCode.getCodeDesc()));
        this.code = errorCode.getCode();
        this.message = errorCode.getCodeDesc();
    }

    /**
     * Construction.
     *
     * @param code
     *            the code
     * @param message
     *            reason
     */
    public GovernanceException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
