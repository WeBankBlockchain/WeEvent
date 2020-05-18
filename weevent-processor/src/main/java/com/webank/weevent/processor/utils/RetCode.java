package com.webank.weevent.processor.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
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

}