package com.webank.weevent.governance.enums;

public enum PayloadEnum {

    JSON(1, "json");

    private Integer code;
    private String value;

    PayloadEnum(Integer code, String value) {
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
        for (PayloadEnum isDeleteEnum : PayloadEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
