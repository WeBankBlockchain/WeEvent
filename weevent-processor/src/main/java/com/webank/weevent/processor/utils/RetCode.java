package com.webank.weevent.processor.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RetCode {
    private int errorCode;
    private String errorMsg;

    private RetCode(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static RetCode mark(int errorCode, String errorMsg) {
        return new RetCode(errorCode, errorMsg);
    }

    public static RetCode mark(Integer errorCode) {
        return new RetCode(errorCode, null);
    }

    @Override
    public String toString() {
        return "RetCode [errorCode=" + errorCode + ", errorMsg=" + errorMsg + "]";
    }


}