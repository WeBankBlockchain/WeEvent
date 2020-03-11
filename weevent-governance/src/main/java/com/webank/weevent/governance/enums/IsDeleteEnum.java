package com.webank.weevent.governance.enums;

import lombok.Getter;

@Getter
public enum IsDeleteEnum {

    NOT_DELETED(0L, "not deleted");

    private Long code;
    private String value;

    IsDeleteEnum(Long code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValueByCode(Long code) {
        for (IsDeleteEnum isDeleteEnum : IsDeleteEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}