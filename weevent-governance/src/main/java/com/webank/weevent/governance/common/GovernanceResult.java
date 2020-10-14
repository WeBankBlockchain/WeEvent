package com.webank.weevent.governance.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GovernanceResult<T> {

	// response code
    private Integer code;

    // response msg
    private String message;

    // response data
    private T data;

    // total number;
    // private Integer totalCount;


    public static <T> GovernanceResult<T> build(Integer code, String message, T data) {
        return new GovernanceResult<T>(code, message, data);
    }

    public static <T> GovernanceResult<T> ok(T data) {
        return new GovernanceResult<T>(data);
    }

    public static <T> GovernanceResult<T> ok() {
        return new GovernanceResult<T>(0,"OK",null);
    }

    public GovernanceResult() {

    }

    public static <T> GovernanceResult<T> build(Integer code, String message) {
        return new GovernanceResult<T>(code, message, null);
    }

    public GovernanceResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public GovernanceResult(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getCodeDesc();
        this.data = null;
    }

    public GovernanceResult(T data) {
        this.code = 0;
        this.message = "OK";
        this.data = data;
    }
}
