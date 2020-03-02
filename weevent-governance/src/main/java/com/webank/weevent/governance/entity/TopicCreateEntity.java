package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.TopicBase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
public class TopicCreateEntity extends TopicBase {

    private String topic;

    private String groupId;



}
