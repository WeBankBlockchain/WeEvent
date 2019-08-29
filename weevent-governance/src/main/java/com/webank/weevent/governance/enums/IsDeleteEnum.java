package com.webank.weevent.governance.enums;

public enum IsDeleteEnum {

    NOT_DELETED(0, "not_deleted"),
    DELETED(1, "deleted");

    private Integer code;
    private String value;

    IsDeleteEnum(Integer code, String value) {
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
        for (IsDeleteEnum isDeleteEnum : IsDeleteEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
