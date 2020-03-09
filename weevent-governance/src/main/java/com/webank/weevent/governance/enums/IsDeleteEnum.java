package com.webank.weevent.governance.enums;

public enum IsDeleteEnum {

    NOT_DELETED(0L, "not deleted");

    private Long code;
    private String value;

    IsDeleteEnum(Long code, String value) {
        this.code = code;
        this.value = value;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueByCode(Long code) {
        for (IsDeleteEnum isDeleteEnum : IsDeleteEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}