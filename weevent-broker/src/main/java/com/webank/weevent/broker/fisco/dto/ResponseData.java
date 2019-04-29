package com.webank.weevent.broker.fisco.dto;

import com.webank.weevent.sdk.ErrorCode;

import lombok.Data;

/**
 * The internal base response result class.
 */
@Data
public class ResponseData<T> {

    private T result;
    private Integer errorCode = ErrorCode.SUCCESS.getCode();
    private String errorMessage = ErrorCode.SUCCESS.getCodeDesc();

    /**
     * Instantiates a new response data.
     */
    public ResponseData() {
    }

    public ResponseData(T result) {
        this.result = result;
    }

    /**
     * Instantiates a new response data.
     *
     * @param result the result
     * @param errorCode the return code
     */
    public ResponseData(T result, ErrorCode errorCode) {
        this.result = result;
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getCodeDesc();
    }
}
