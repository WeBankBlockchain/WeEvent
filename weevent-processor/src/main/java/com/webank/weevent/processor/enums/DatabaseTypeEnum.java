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

    public String getValueByCode(String code) {
        for (DatabaseTypeEnum isDeleteEnum : DatabaseTypeEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}