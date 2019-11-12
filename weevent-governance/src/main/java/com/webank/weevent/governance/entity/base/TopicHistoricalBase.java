package com.webank.weevent.governance.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class TopicHistoricalBase extends BaseEntity {

    private Integer brokerId;

    private Integer userId;

    private String topicName;

    private String groupId;

    private String eventId;

}
