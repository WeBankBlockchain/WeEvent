package com.webank.weevent.governance.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class GovernanceResult {

    // response status
    private Integer status;

    // response msg
    private String msg;

    // response data
    private Object data;

    // total number;
    private Integer totalCount;


    public static GovernanceResult build(Integer status, String msg, Object data) {
        return new GovernanceResult(status, msg, data);
    }

    public static GovernanceResult ok(Object data) {
        return new GovernanceResult(data);
    }

    public static GovernanceResult ok() {
        return new GovernanceResult(200,"OK",null);
    }

    public GovernanceResult() {

    }

    public static GovernanceResult build(Integer status, String msg) {
        return new GovernanceResult(status, msg, null);
    }

    public GovernanceResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public GovernanceResult(ErrorCode errorCode) {
        this.status = errorCode.getCode();
        this.msg = errorCode.getCodeDesc();
        this.data = null;
    }

    public GovernanceResult(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }
}
