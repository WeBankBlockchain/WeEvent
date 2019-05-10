package com.webank.weevent.governance.service;

import java.util.List;

import com.webank.weevent.governance.entity.Host;

/**
 * topic service interface
 * @since 2018/12/18
 */
public interface TopicService {
	//get topics by page
	Object getTopics(Integer pageIndex, Integer pageSize);
	
	//get topics by page
	Object getTopics(Integer id,Integer pageIndex, Integer pageSize);

	//open a new topic
	Object open(String topic,String creater);

    //close topic
    Object close(Integer id,String topic);
    
    // get machine list
    List<Host> getHost();

    //open new topic
	Object open(Integer id, String topic, String creater);
	
}
