package com.webank.weevent.governance.enums;

import lombok.Getter;

@Getter
public enum CheckTypeEnum {

    CHECK_DATABASE(1, "check database"),
    CHECK_TABLE(2, "check database");

    private Integer code;
    private String value;

    CheckTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValueByCode(Integer code) {
        for (CheckTypeEnum isDeleteEnum : CheckTypeEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}