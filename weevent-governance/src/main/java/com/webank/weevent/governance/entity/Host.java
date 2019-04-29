package com.webank.weevent.governance.entity;

import java.util.Date;
import lombok.Data;

/**
 * Host class
 *
 * @since 2019/02/11
 */
@Data
public class Host {

    private  Date time;
    private String hostName;
    private float usageSystem;
}
