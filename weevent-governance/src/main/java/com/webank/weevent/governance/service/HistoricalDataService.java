package com.webank.weevent.governance.service;

import java.util.List;

import com.webank.weevent.governance.entity.HistoricalDataEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.HistoricalDataMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HistoricalDataService {

    @Autowired
    private HistoricalDataMapper historicalDataMapper;

    @Autowired
    private CommonService commonService;

    public List<HistoricalDataEntity> historicalDataList(HistoricalDataEntity historicalDataEntity, HttpRequest httpRequest,
                                                         HttpResponse httpResponse)throws GovernanceException {

        return  null;
    }

}
