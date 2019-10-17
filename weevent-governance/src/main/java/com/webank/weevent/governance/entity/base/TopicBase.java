package com.webank.weevent.governance.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TopicBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class TopicBase extends BaseEntity {

    private Integer isDelete;

    private Integer brokerId;

    private String groupId;

    private String topicName;

    private String creater;

    private String description;

}
