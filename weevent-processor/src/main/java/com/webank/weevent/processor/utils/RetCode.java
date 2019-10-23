package com.webank.weevent.processor.utils;

import lombok.Data;

@Data
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

//    public Integer getErrorCode() {
//        return errorCode;
//    }
//
//    public String getErrorMsg() {
//        return errorMsg;
//    }
//
//    public void setErrorCode(int errorCode) {
//        this.errorCode = errorCode;
//    }
//
//    public void setErrorMsg(String errorMsg) {
//        this.errorMsg = errorMsg;
//    }

    @Override
    public String toString() {
        return "RetCode [errorCode=" + errorCode + ", errorMsg=" + errorMsg + "]";
    }


}