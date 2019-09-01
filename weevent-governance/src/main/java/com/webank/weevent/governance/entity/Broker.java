package com.webank.weevent.governance.entity;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

/**
 * Broker class
 *
 * @since 2019/04/28
 */
@Data
@ToString
public class Broker {

    private Integer id;

    @Min(1)
    private Integer userId;

    private String name;

    @URL
    private String brokerUrl;

    @URL
    private String webaseUrl;

    private Date lastUpdate;

    private Integer isDelete;

    private List<Integer> userIdList;

}
