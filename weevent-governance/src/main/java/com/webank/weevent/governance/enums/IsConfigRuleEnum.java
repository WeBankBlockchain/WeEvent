package com.webank.weevent.governance.enums;

public enum IsConfigRuleEnum {

    CONFIGURED("1", "configured"),
    NOT_CONFIGURED("2", "not configured");

    private String code;
    private String value;

    IsConfigRuleEnum(String code, String value) {
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
        for (IsConfigRuleEnum isDeleteEnum : IsConfigRuleEnum.values()) {
            if (isDeleteEnum.code.equals(code)) {
                return isDeleteEnum.value;
            }
        }
        return null;
    }


}
