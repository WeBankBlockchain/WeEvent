package com.webank.weevent.broker.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {

    ALL("0"),
    PUBLISH("publish"),
    SUBSCRIBE("subscribe");

    private String code;

    PermissionEnum(String code) {
        this.code = code;
    }

}