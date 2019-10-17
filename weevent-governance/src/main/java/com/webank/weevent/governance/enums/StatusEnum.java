package com.webank.weevent.governance.enums;

public enum StatusEnum {

    NOT_STARTED(0, "not started"),
    RUNNING(1, "running"),
    IS_DELETED(2, "is deleted");

    private Integer code;
    private String value;

    StatusEnum(Integer code, String value) {
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
        for (StatusEnum isDeleteEnum : StatusEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
