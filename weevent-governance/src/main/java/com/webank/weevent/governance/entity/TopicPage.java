package com.webank.weevent.governance.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TopicEntity data in page list.
 *
 * @since 2019/02/11
 */
@Setter
@Getter
public class TopicPage {

    private Integer total;

    private Integer pageIndex;

    private Integer pageSize;

    private List<TopicEntity> topicInfoList;
}
