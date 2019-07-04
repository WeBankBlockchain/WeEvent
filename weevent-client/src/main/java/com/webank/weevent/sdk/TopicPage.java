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
    private Integer total;
    private Integer pageIndex;
    private Integer pageSize;
    private List<TopicInfo> topicInfoList = new ArrayList<>();
}
