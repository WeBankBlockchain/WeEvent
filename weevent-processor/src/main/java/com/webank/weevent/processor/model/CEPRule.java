package com.webank.weevent.processor.model;

import java.util.Date;

public class CEPRule {
    private String id;

    private String ruleName;

    private String fromDestination;

    private String brokerUrl;

    private String payload;

    private Integer payloadType;

    private String selectField;

    private String conditionField;

    private Integer conditionType;

    private String toDestination;

    private String databaseUrl;

    private Date createdTime;

    private Integer status;

    private String errorDestination;

    private String errorCode;

    private String errorMessage;

    private Date updatedTime;

    public CEPRule(String id, String ruleName, String fromDestination, String brokerUrl, String payload, Integer payloadType, String selectField, String conditionField, Integer conditionType, String toDestination, String databaseUrl, Date createdTime, Integer status, String errorDestination, String errorCode, String errorMessage, Date updatedTime) {
        this.id = id;
        this.ruleName = ruleName;
        this.fromDestination = fromDestination;
        this.brokerUrl = brokerUrl;
        this.payload = payload;
        this.payloadType = payloadType;
        this.selectField = selectField;
        this.conditionField = conditionField;
        this.conditionType = conditionType;
        this.toDestination = toDestination;
        this.databaseUrl = databaseUrl;
        this.createdTime = createdTime;
        this.status = status;
        this.errorDestination = errorDestination;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.updatedTime = updatedTime;
    }

    public CEPRule() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName == null ? null : ruleName.trim();
    }

    public String getFromDestination() {
        return fromDestination;
    }

    public void setFromDestination(String fromDestination) {
        this.fromDestination = fromDestination == null ? null : fromDestination.trim();
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl == null ? null : brokerUrl.trim();
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload == null ? null : payload.trim();
    }

    public Integer getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(Integer payloadType) {
        this.payloadType = payloadType;
    }

    public String getSelectField() {
        return selectField;
    }

    public void setSelectField(String selectField) {
        this.selectField = selectField == null ? null : selectField.trim();
    }

    public String getConditionField() {
        return conditionField;
    }

    public void setConditionField(String conditionField) {
        this.conditionField = conditionField == null ? null : conditionField.trim();
    }

    public Integer getConditionType() {
        return conditionType;
    }

    public void setConditionType(Integer conditionType) {
        this.conditionType = conditionType;
    }

    public String getToDestination() {
        return toDestination;
    }

    public void setToDestination(String toDestination) {
        this.toDestination = toDestination == null ? null : toDestination.trim();
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl == null ? null : databaseUrl.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorDestination() {
        return errorDestination;
    }

    public void setErrorDestination(String errorDestination) {
        this.errorDestination = errorDestination == null ? null : errorDestination.trim();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode == null ? null : errorCode.trim();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}