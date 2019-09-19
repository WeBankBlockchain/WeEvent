package com.webank.weevent.protocol.rest;

import com.webank.weevent.sdk.ErrorCode;

import lombok.Data;

/**
 * The base response result class.
 *
 * @author v_wbhwliu
 * @version 1.0
 * @since 2019/9/12
 */
@Data
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
     * The total number of data
     */
    private Integer totalCount;

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
