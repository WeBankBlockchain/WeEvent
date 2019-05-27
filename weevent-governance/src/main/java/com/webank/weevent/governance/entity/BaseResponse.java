package com.webank.weevent.governance.entity;

import lombok.Data;

import com.webank.weevent.governance.code.RetCode;

/**
 * Entity class of response info.
 */
@Data
public class BaseResponse {

    private int code;
    private String message;
    private Object data;

    public BaseResponse() {
    }

    public BaseResponse(RetCode retcode) {
        this.code = retcode.getCode();
        this.message = retcode.getMsg();
    }
}
