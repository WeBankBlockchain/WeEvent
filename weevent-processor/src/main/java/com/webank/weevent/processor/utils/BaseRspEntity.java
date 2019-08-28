package com.webank.weevent.processor.utils;

/**
 * Entity class of response info.
 */

/**
 * 返回类的基类
 */
public class BaseRspEntity {
    private int errorCode;//返回码
    private String errorMsg;//返回信息
    private Object data;//返回数据

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