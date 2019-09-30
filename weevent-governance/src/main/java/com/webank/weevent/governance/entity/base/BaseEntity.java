package com.webank.weevent.governance.entity.base;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class BaseEntity {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
    /**
     * primary key
     */
    private Integer id;

    private Date createDate;

    private Date lastUpdate;

  /*  public void setCreateDate(Date createDate) throws ParseException {
        if (createDate == null) {
            this.createDate = null;
            return;
        }
        String format = simpleDateFormat.format(createDate);
        this.createDate = simpleDateFormat.parse(format);
    }*/
}