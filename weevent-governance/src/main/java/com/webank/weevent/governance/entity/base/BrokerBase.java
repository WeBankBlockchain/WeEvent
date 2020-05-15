package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Min;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * BrokerBase class
 *
 * @since 2019/10/15
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class BrokerBase extends BaseEntity {

    @Min(1)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "name")
    private String name;

    @URL
    @Column(name = "broker_url", columnDefinition = "varchar(64)")
    private String brokerUrl;

    //0 means not deleted ,others means deleted
    @Column(name = "delete_at",nullable = false, columnDefinition = "BIGINT(16)")
    private Long deleteAt = 0L;

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl == null ? null : brokerUrl.trim();
    }


}
