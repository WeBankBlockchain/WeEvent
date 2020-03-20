package com.webank.weevent.governance.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {

    NOT_STARTED(0, "not started"),
    RUNNING(1, "running"),
    IS_DELETED(2, "is deleted");

    private Integer code;
    private String value;

    StatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

}