package com.webank.weevent.governance.enums;

public enum ConditionTypeEnum {

    TOPIC(1, "topic"),
    DATABASE(2, "database");

    private Integer code;
    private String value;

    ConditionTypeEnum(Integer code, String value) {
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
        for (ConditionTypeEnum isDeleteEnum : ConditionTypeEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
