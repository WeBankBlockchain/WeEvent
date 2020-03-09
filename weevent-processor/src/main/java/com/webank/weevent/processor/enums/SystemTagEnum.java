package com.webank.weevent.processor.enums;

public enum SystemTagEnum {

    TOPIC("1", "built-in system "),
    DATABASE("2", "user created");

    private String code;
    private String value;

    SystemTagEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueByCode(Integer code) {
        for (SystemTagEnum isDeleteEnum : SystemTagEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}