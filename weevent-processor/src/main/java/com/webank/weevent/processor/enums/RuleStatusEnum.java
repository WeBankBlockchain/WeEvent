package com.webank.weevent.processor.enums;

import lombok.Getter;

@Getter
public enum RuleStatusEnum {

    NOT_STARTED(0, "not started"),
    RUNNING(1, "running"),
    IS_DELETED(2, "is deleted");

    private Integer code;
    private String value;

    RuleStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValueByCode(Integer code) {
        for (RuleStatusEnum isDeleteEnum : RuleStatusEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}