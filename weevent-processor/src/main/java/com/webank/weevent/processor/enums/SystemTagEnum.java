package com.webank.weevent.processor.enums;

import lombok.Getter;

@Getter
public enum SystemTagEnum {

    TOPIC("1", "built-in system "),
    DATABASE("2", "user created");

    private String code;
    private String value;

    SystemTagEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValueByCode(Integer code) {
        for (SystemTagEnum isDeleteEnum : SystemTagEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}