package com.webank.weevent.client;


/**
 * Broker's exception.
 * code in (100000, 200000) meanings client error.
 * code in (200000, 300000) meanings server error.
 *
 * @author matthewliu
 * @since 2018/11/08
 */
public class BrokerException extends Exception {
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
     * @param code the code
     * @param message the message
     * @return java.lang.String
     */
    private static String getText(int code, String message) {
        return "Code: " +
                code +
                ", Message: " +
                message +
                "\nFor more information, please visit wiki: " +
                WIKIUrl;
    }

    /**
     * Construction.
     *
     * @param message the message
     * @param cause the cause
     */
    public BrokerException(String message, Throwable cause) {
        super(getText(-1, message), cause);
        this.code = -1;
        this.message = message;
    }

    /**
     * Construction.
     *
     * @param message the message
     */
    public BrokerException(String message) {
        super(getText(-1, message));
        this.code = -1;
        this.message = message;
    }

    /**
     * Construction.
     *
     * @param errorCode the code and message
     */
    public BrokerException(ErrorCode errorCode) {
        super(getText(errorCode.getCode(), errorCode.getCodeDesc()));
        this.code = errorCode.getCode();
        this.message = errorCode.getCodeDesc();
    }

    /**
     * Construction.
     *
     * @param code the code
     * @param message reason
     */
    public BrokerException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
