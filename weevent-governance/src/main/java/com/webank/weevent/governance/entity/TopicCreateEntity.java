package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.TopicBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TopicCreateEntity extends TopicBase {

    private String topic;

    private String groupId;

}
