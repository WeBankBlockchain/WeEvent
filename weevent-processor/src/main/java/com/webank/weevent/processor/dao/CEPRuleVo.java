package com.webank.weevent.processor.dao;

import java.util.ArrayList;


import com.webank.weevent.processor.model.CEPRule;

public class CEPRuleVo {
    private Integer total;

    private Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    Integer pageIndex;

    Integer pageSize;
    private ArrayList<CEPRule> cEPRuleList;

    public ArrayList<CEPRule> getcEPRuleList() {
        return cEPRuleList;
    }

    public void setcEPRuleList(ArrayList<CEPRule> cEPRuleList) {
        this.cEPRuleList = cEPRuleList;
    }
}
