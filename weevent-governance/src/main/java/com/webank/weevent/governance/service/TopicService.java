package com.webank.weevent.governance.service;

/**
 * topic service interface
 * @since 2018/12/18
 */
public interface TopicService {
	
	//get topics by page
	Object getTopics(Integer id,Integer pageIndex, Integer pageSize);

    //close topic
    Object close(Integer id,String topic);

    //open new topic
	Object open(Integer id, String topic, String creater);
	
}
