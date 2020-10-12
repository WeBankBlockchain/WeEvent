package com.webank.weevent.governance.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GovernanceResponse<T> {

	// response code
    private Integer code;

    // response msg
    private String message;

    // response data
    private T data;

    // total number;
    // private Integer totalCount;


    public static <T> GovernanceResponse<T> build(Integer code, String message, T data) {
        return new GovernanceResponse<T>(code, message, data);
    }

    public static <T> GovernanceResponse<T> ok(T data) {
        return new GovernanceResponse<T>(data);
    }

    public static <T> GovernanceResponse<T> ok() {
        return new GovernanceResponse<T>(0,"OK",null);
    }

    public GovernanceResponse() {

    }

    public static <T> GovernanceResponse<T> build(Integer code, String message) {
        return new GovernanceResponse<T>(code, message, null);
    }

    public GovernanceResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public GovernanceResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getCodeDesc();
        this.data = null;
    }

    public GovernanceResponse(T data) {
        this.code = 0;
        this.message = "OK";
        this.data = data;
    }
}
