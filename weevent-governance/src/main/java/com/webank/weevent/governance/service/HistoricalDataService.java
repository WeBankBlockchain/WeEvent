package com.webank.weevent.governance.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.HistoricalDataEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.HistoricalDataMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class HistoricalDataService {

    @Autowired
    private HistoricalDataMapper historicalDataMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CookiesTools cookiesTools;


    private final static String simpleDateFormat = "YYYY-MM-dd";


    public Map<String, List<Integer>> historicalDataList(HistoricalDataEntity historicalDataEntity, HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(httpRequest, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            Boolean flag = permissionService.verifyPermissions(historicalDataEntity.getBrokerId(), accountId);
            if (!flag) {
                throw new GovernanceException(ConstantCode.ACCESS_DENIED.getMsg());
            }
            Map<String, List<Integer>> returnMap = new HashMap<>();
            List<HistoricalDataEntity> historicalDataEntities = historicalDataMapper.historicalDataList(historicalDataEntity);
            if (CollectionUtils.isEmpty(historicalDataEntities)) {
                return null;
            }
            if (historicalDataEntity.getBeginDate() == null || historicalDataEntity.getEndDate() == null) {
                throw new GovernanceException("beginDate or endDate is empty");
            }
            Date beginDate = historicalDataEntity.getBeginDate();
            Date endDate = historicalDataEntity.getEndDate();

            historicalDataEntity.setBeginDate(DateUtils.parseDate(DateFormatUtils.format(beginDate, simpleDateFormat), simpleDateFormat));
            historicalDataEntity.setEndDate(DateUtils.parseDate(DateFormatUtils.format(endDate, simpleDateFormat), simpleDateFormat));
            //deal data
            Map<String, List<HistoricalDataEntity>> map = new HashMap<>();
            historicalDataEntities.forEach(it -> {
                map.merge(it.getTopicName(), new ArrayList<>(Collections.singletonList(it)), this::mergeCollection);
            });
            List<String> listDate;
            listDate = listDate(historicalDataEntity.getBeginDate(), historicalDataEntity.getEndDate());

            map.forEach((k, v) -> {
                Map<String, Integer> eventCountMap = new HashMap<>();
                for (HistoricalDataEntity dataEntity : v) {
                    eventCountMap.put(DateFormatUtils.format(dataEntity.getCreateDate(), simpleDateFormat), dataEntity.getEventCount());
                }
                List<Integer> integerList = new ArrayList<>();
                for (String date : listDate) {
                    //Make sure there is data every day, even if it is zero
                    integerList.add(eventCountMap.get(date) == null ? 0 : eventCountMap.get(date));
                }
                returnMap.put(k, integerList);
            });
            return returnMap;
        } catch (Exception e) {
            log.info("get historicalDataEntity fail", e);
            throw new GovernanceException("get historicalDataEntity fail", e);
        }

    }

    private List<HistoricalDataEntity> mergeCollection(List<HistoricalDataEntity> a, List<HistoricalDataEntity> b) {
        List<HistoricalDataEntity> list = new ArrayList<>();
        list.addAll(a);
        list.addAll(b);
        return list;
    }

    private List<String> listDate(Date beginDate, Date endDate) {
        List<String> dateList = new ArrayList<>();
        dateList.add(DateFormatUtils.format(beginDate, simpleDateFormat));
        Calendar calBegin = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        calBegin.setTime(beginDate);
        while (endDate.after(calBegin.getTime())) {
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            dateList.add(DateFormatUtils.format(calBegin.getTime(), simpleDateFormat));
        }
        return dateList;
    }


}
