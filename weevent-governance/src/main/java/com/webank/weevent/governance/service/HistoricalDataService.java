package com.webank.weevent.governance.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.HistoricalDataEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.HistoricalDataMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HistoricalDataService {

    @Autowired
    private HistoricalDataMapper historicalDataMapper;

    private final static String simpleDateFormat = "YYYY-MM-dd";


    public Map<String, List<Integer>> historicalDataList(HistoricalDataEntity historicalDataEntity, HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) throws GovernanceException {
        try {
            if (historicalDataEntity.getEndDate() == null) {
                historicalDataEntity.setEndDate(new Date());
            }
            Map<String, List<Integer>> returnMap = new HashMap<>();

            //get begin time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(historicalDataEntity.getEndDate());
            calendar.add(Calendar.DATE, -6);
            historicalDataEntity.setBeginDate(calendar.getTime());
            List<HistoricalDataEntity> historicalDataEntities = historicalDataMapper.historicalDataList(historicalDataEntity);
            //deal data
            Map<String, List<HistoricalDataEntity>> map = new HashMap<>();
            List<Date> dateList = new ArrayList<>();
            historicalDataEntities.forEach(it -> {
                map.merge(it.getTopicName(), new ArrayList<>(Arrays.asList(it)), (a, b) -> this.mergeCollection(a, b));
            });
            List<String> listDate = listDate(historicalDataEntity.getBeginDate(), historicalDataEntity.getEndDate());

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
        dateList.add(DateFormatUtils.format(beginDate, this.simpleDateFormat));
        Calendar calBegin = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        calBegin.setTime(beginDate);
        while (endDate.after(calBegin.getTime())) {
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            dateList.add(DateFormatUtils.format(calBegin.getTime(), this.simpleDateFormat));
        }
        return dateList;
    }


}
