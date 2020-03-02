package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.common.RetCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity class of response info.
 */
@Setter
@Getter
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
