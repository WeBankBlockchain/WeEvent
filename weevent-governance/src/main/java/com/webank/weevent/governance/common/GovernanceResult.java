package com.webank.weevent.governance.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GovernanceResult {

    // response status
    private Integer status;

    // response message
    private String message;

    // response data
    private Object data;

    // total number;
    private Integer totalCount;


    public static GovernanceResult build(Integer status, String message, Object data) {
        return new GovernanceResult(status, message, data);
    }

    public static GovernanceResult ok(Object data) {
        return new GovernanceResult(data);
    }

    public static GovernanceResult ok() {
        return new GovernanceResult(200,"OK",null);
    }

    public GovernanceResult() {

    }

    public static GovernanceResult build(Integer status, String message) {
        return new GovernanceResult(status, message, null);
    }

    public GovernanceResult(Integer status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public GovernanceResult(ErrorCode errorCode) {
        this.status = errorCode.getCode();
        this.message = errorCode.getCodeDesc();
        this.data = null;
    }

    public GovernanceResult(Object data) {
        this.status = 200;
        this.message = "OK";
        this.data = data;
    }
}
