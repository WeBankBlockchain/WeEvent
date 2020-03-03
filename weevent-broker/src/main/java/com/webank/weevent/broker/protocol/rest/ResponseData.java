package com.webank.weevent.broker.protocol.rest;

import com.webank.weevent.client.ErrorCode;

import lombok.Getter;
import lombok.Setter;

/**
 * The base response result class.
 *
 * @author v_wbhwliu
 * @version 1.0
 * @since 2019/9/12
 */
@Getter
@Setter
public class ResponseData<T> {

    /**
     * The generic type result object.
     */
    private T data;

    /**
     * The error code.
     */
    private int code;

    /**
     * The error message.
     */
    private String message;


    /**
     * set a ErrorCode type errorCode.
     *
     * @param errorCode the errorCode
     */
    public void setErrorCode(ErrorCode errorCode) {
        if (errorCode != null) {
            this.code = errorCode.getCode();
            this.message = errorCode.getCodeDesc();
        }
    }

}
