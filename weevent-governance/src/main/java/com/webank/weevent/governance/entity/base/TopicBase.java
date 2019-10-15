package com.webank.weevent.governance.entity.base;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TopicEntity class
 *
 * @since 2019/02/11
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
