package com.webank.weevent.processor.enums;

import lombok.Getter;

@Getter
public enum SystemTagEnum {

    BUILT_IN_SYSTEM("1", "built-in system "),
    USER_CREATED("2", "user created");

    private String code;
    private String value;

    SystemTagEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValueByCode(String code) {
        for (SystemTagEnum isDeleteEnum : SystemTagEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}