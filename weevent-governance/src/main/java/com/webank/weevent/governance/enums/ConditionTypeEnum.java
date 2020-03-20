package com.webank.weevent.governance.enums;

import lombok.Getter;

@Getter
public enum ConditionTypeEnum {

    TOPIC(1, "topic"),
    DATABASE(2, "database");

    private Integer code;
    private String value;

    ConditionTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

}