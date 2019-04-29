package com.webank.weevent.governance.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;

import com.webank.weevent.governance.configuration.InfluxDBConnect;
import com.webank.weevent.governance.entity.Host;
import com.webank.weevent.governance.entity.Topic;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.mapper.TopicInfoMapper;
import com.webank.weevent.governance.service.TopicService;

/**
 * topic service
 * @since 2018/12/18
 */
@Service
@Slf4j
public class TopicServiceImpl implements TopicService{

    @Autowired
    TopicInfoMapper topicInfoMapper;
    
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    InfluxDBConnect influxDBConnect;
    
    @Value("${weevent.url}")
    private String url;
    
    @Override
    public Object getTopics(Integer pageIndex, Integer pageSize) {
        
        //get eventbroker url
        String url = this.url + "/rest/list";
        url = url + "?pageIndex=" + pageIndex+"&pageSize="+pageSize;
        log.info("pageIndex: " + pageIndex + " pageSize: "+pageSize);
        String json = restTemplate.getForEntity(url, String.class).getBody();
        log.info("json="+json+" url="+url);
        if(json != null) {
            List<Topic> topicList = null;
            TopicPage topicPage = null;
            try {
                topicPage = JSONArray.parseObject(json,TopicPage.class);
                topicList = topicPage.getTopicInfoList();
                if(topicList.size() > 0) {
                    for (int i =0;i < topicList.size();i++) {
                        String creater = topicInfoMapper.getCreaterByName(topicList.get(i).getTopicName());
                        if(!StringUtils.isEmpty(creater)) {
                            topicList.get(i).setCreater(creater);
                        }
                    }
                }
                
                topicPage.setTopicInfoList(topicList);
                
                return topicPage;
            } catch (Exception e) {
                return JSONArray.parseObject(json);
            }
            
        }
        return null;
    }

    @Override
    public Object open(String topic,String creater) {
        
        String url = this.url + "/rest/open?topic=" +topic;
        log.info("topic: "+ topic +" creater: " + creater);
        Object response = restTemplate.getForEntity(url, Object.class).getBody();
        if((response instanceof Boolean) && (Boolean)response) {
            Boolean result = false;
            result = topicInfoMapper.openTopic(topic,creater);
            return result;
        }
        
        return response;
    }

    @Override
    public Boolean close(String topic) {
        String url = this.url + "/rest/close?topic=" + topic;
        Boolean response = restTemplate.getForEntity(url, Boolean.class).getBody();
        return response;
    }

    @Override
    public List<Host> getHost() {
        List<Host> list = new ArrayList<Host>();
        if(influxDBConnect.getEnabled() == null || !influxDBConnect.getEnabled().equals("true")) {
            return list;
        }
        QueryResult response = influxDBConnect.query("select time,host,usage_system from autogen.cpu limit 2");
        List<Result> results = response.getResults();
        Result result = results.get(0);
        Series series = result.getSeries().get(0);
        List<List<Object>> values = series.getValues();
        for (List<Object> value : values) {
            Host host = new Host();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try {
                Date date=simpleDateFormat.parse(value.get(0).toString());
                host.setTime(date);
                host.setHostName(value.get(1).toString());
                host.setUsageSystem(Float.parseFloat(value.get(2).toString()));
            } catch (ParseException e) {
                log.error(e.getMessage());
            }
            list.add(host);
        }
        return list;
    }

}
