package com.webank.weevent.governance.enums;

public enum IsCreatorEnum {

    CREATOR("1", "creator"),
    AUTHORIZED("2", "authorized");

    private String code;
    private String value;

    IsCreatorEnum(String code, String value) {
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
        for (IsCreatorEnum isDeleteEnum : IsCreatorEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
