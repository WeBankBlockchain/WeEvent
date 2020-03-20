package com.webank.weevent.governance.enums;

import lombok.Getter;

@Getter
public enum PayloadEnum {

    JSON(1, "json");

    private Integer code;
    private String value;

    PayloadEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }


}