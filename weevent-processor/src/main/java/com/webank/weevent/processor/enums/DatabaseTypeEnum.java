package com.webank.weevent.processor.enums;

public enum DatabaseTypeEnum {

    H2_DATABASE(1, "h2 database"),
    MYSQL_DATABASE(2, "mysql database");

    private Integer code;
    private String value;

    DatabaseTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueByCode(Integer code) {
        for (DatabaseTypeEnum isDeleteEnum : DatabaseTypeEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}