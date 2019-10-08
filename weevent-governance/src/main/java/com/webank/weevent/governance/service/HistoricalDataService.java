package com.webank.weevent.governance.service;

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

    @Autowired
    private CommonService commonService;

    public List<HistoricalDataEntity> historicalDataList(HistoricalDataEntity historicalDataEntity, HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) throws GovernanceException {
        try {
            List<HistoricalDataEntity> historicalDataEntities = historicalDataMapper.historicalDataList(historicalDataEntity);
            return historicalDataEntities;
        } catch (Exception e) {
            log.info("get all historicalDataEntity fail", e);
            throw new GovernanceException("get all historicalDataEntity fail", e);
        }

    }

}
