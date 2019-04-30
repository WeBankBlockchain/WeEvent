package com.webank.weevent.governance.entity;

import java.util.Date;
import lombok.Data;

/**
 * Topic class
 *
 * @since 2019/02/11
 */
@Data
public class Topic {

    private String topicName;
    
    private String creater;
    
    private String topicAddress;
    
    private String senderAddress;
    
    private Date createdTimestamp;
}
