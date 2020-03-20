package com.webank.weevent.governance.enums;

import lombok.Getter;

@Getter
public enum IsCreatorEnum {

    CREATOR("1", "creator"),
    AUTHORIZED("2", "authorized");

    private String code;
    private String value;

    IsCreatorEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

}