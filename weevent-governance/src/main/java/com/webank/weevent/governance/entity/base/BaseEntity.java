package com.webank.weevent.governance.entity.base;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * BaseEntity class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class BaseEntity {

    /**
     * primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @org.hibernate.annotations.CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date", updatable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

    @org.hibernate.annotations.UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Date lastUpdate;


}