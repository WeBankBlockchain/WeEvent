package com.webank.weevent.broker.enums;

import lombok.Getter;

@Getter
public enum IsAuthEnum {

    ON("ON"),
	OFF("OFF");

    private String value;

    IsAuthEnum(String value) {
        this.value = value;
    }


}