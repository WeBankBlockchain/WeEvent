package com.webank.weevent.processor.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class CEPRule implements Serializable {
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

    private String brokerId;

    private String userId;

    private String groupId;


    private String systemTag;

    private String offSet;
}