package com.webank.weevent.governance.entity;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Topic data in page list.
 *
 * @since 2019/02/11
 */
@Data
public class TopicPage {
    Integer total;
    Integer pageIndex;
    Integer pageSize;
    List<Topic> topicInfoList = new ArrayList<>();
}
