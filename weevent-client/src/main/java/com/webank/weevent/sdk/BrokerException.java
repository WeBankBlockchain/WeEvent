package com.webank.weevent.sdk;


/**
 * Broker's exception.
 * code in (100000, 200000) meanings client side error.
 * code in (200000, 300000) meanings server side error.
 *
 * @author matthewliu
 * @since 2018/11/08
 */
public class BrokerException extends Exception {
    private final static String WIKIUrl = "https://github.com/webankopen/weevent-broker/wiki/faq";

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
