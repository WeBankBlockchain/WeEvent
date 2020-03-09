package com.webank.weevent.governance.enums;

public enum CheckTypeEnum {

    CHECK_DATABASE(1, "check database"),
    CHECK_TABLE(2, "check database");

    private Integer code;
    private String value;

    CheckTypeEnum(Integer code, String value) {
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
        for (CheckTypeEnum isDeleteEnum : CheckTypeEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}