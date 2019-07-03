package com.webank.weevent.governance.entity;

import java.util.Date;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.URL;
import lombok.Data;
import lombok.ToString;

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
}
