package com.webank.weevent.processor.utils;

import lombok.Value;

/**
 * Entity class of response info.
 */

public class BaseRspEntity {
    private int errorCode;
    private String errorMsg;
    private Object data ;

    public BaseRspEntity(){}

    public BaseRspEntity(int errorCode){
        this.errorCode = errorCode;
    }
    public BaseRspEntity(RetCode rsc){
        this.errorCode = rsc.getErrorCode();
        this.errorMsg = rsc.getErrorMsg();
    }
    public BaseRspEntity(RetCode rsc, Object obj){
        this.errorCode = rsc.getErrorCode();
        this.errorMsg = rsc.getErrorMsg();
        this.data = obj;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}