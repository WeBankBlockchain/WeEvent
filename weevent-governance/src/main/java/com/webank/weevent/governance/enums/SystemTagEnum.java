package com.webank.weevent.governance.enums;

public enum SystemTagEnum {

    BUILT_IN_SYSTEM("1", "built in system"),
    USER_ADDED("2", "user added");

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

    public String getValueByCode(String code) {
        for (SystemTagEnum isDeleteEnum : SystemTagEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
