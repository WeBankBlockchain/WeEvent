package com.webank.weevent.processor.enums;

import lombok.Getter;

@Getter
public enum DatabaseTypeEnum {

    H2_DATABASE("1", "h2 database"),
    MYSQL_DATABASE("2", "mysql database");

    private String code;
    private String value;

    DatabaseTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }


}