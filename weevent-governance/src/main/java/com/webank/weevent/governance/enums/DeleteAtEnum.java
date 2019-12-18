package com.webank.weevent.governance.enums;

public enum DeleteAtEnum {

    NOT_DELETED(0L, "not_deleted");
    private Long code;
    private String value;

    DeleteAtEnum(Long code, String value) {
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
        for (DeleteAtEnum deleteAtEnum : DeleteAtEnum.values()) {
            if (deleteAtEnum.code.equals(code)) {
                return deleteAtEnum.value;
            }
        }
        return null;
    }


}
