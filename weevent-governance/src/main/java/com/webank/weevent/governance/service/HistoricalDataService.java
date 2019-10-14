package com.webank.weevent.governance.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.HistoricalDataEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.HistoricalDataMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HistoricalDataService {

    @Autowired
    private HistoricalDataMapper historicalDataMapper;


    public List<HistoricalDataEntity> historicalDataList(HistoricalDataEntity historicalDataEntity, HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) throws GovernanceException {
        try {
            if (historicalDataEntity.getEndDate() == null) {
                historicalDataEntity.setEndDate(new Date());
            }
            //get begin time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(historicalDataEntity.getEndDate());
            calendar.add(Calendar.DATE, -7);
            historicalDataEntity.setBeginDate(calendar.getTime());
            List<HistoricalDataEntity> historicalDataEntities = historicalDataMapper.historicalDataList(historicalDataEntity);
            return historicalDataEntities;
        } catch (Exception e) {
            log.info("get historicalDataEntity fail", e);
            throw new GovernanceException("get historicalDataEntity fail", e);
        }

    }

}
