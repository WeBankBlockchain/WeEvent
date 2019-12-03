package com.webank.weevent.governance.enums;

public enum DeleteAtEnum {

    NOT_DELETED("0", "not_deleted");
    private String code;
    private String value;

    DeleteAtEnum(String code, String value) {
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
        for (DeleteAtEnum deleteAtEnum : DeleteAtEnum.values()) {
            if (deleteAtEnum.code.equals(code)) {
                return deleteAtEnum.value;
            }
        }
        return null;
    }


}
