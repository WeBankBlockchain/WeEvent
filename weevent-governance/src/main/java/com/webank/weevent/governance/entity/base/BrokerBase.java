package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Min;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;

/**
 * BrokerBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class BrokerBase extends BaseEntity {

    @Min(1)
    @Column(name="user_id")
    private Integer userId;

    @Column(name = "name")
    private String name;

    @URL
    @Column(name = "broker_url")
    private String brokerUrl;

    @Column(name = "webase_url")
    private String webaseUrl;

    @Column(name = "delete_at")
    private String deleteAt;

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl == null ? null : brokerUrl.trim();
    }


}
