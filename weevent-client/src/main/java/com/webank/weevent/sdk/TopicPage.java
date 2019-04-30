package com.webank.weevent.sdk;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Topic data in page list.
 *
 * @author matthewliu
 * @since 2019/02/11
 */
@Data
public class TopicPage {
    Integer total;
    Integer pageIndex;
    Integer pageSize;
    List<TopicInfo> topicInfoList = new ArrayList<>();
}
