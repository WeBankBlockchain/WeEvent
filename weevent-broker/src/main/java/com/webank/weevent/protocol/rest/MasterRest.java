package com.webank.weevent.protocol.rest;

import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Some tasks running in master node.
 *
 * @author matthewliu
 * @version 1.0
 * @since 2018/12/14
 */
@Slf4j
@RequestMapping(value = "/master")
@RestController
public class MasterRest extends RestHA {
    // mqtt_add_inbound_topic/mqtt_remove_inbound_topic control mqtt inbound topic
    @RequestMapping(path = "/mqtt_add_inbound_topic", method = RequestMethod.GET)
    public boolean mqttAddInBoundTopic(@RequestParam(name = "topic") String topic) throws BrokerException {
        checkSupport();
        return this.masterJob.getMqttTopic().mqttAddInBoundTopic(topic, getUrlFormat(this.request));
    }

    @RequestMapping(path = "/mqtt_remove_inbound_topic", method = RequestMethod.GET)
    public boolean mqttRemoveInBoundTopic(@RequestParam(name = "topic") String topic) throws BrokerException {
        checkSupport();
        return this.masterJob.getMqttTopic().mqttRemoveInBoundTopic(topic, getUrlFormat(this.request));
    }

    // mqtt_add_outbound_topic/mqtt_remove_outbound_topic control mqtt outbound topic
    @RequestMapping(path = "/mqtt_add_outbound_topic", method = RequestMethod.GET)
    public boolean mqttAddOutBoundTopic(@RequestParam(name = "topic") String topic) throws BrokerException {
        checkSupport();
        return this.masterJob.getMqttTopic().mqttAddOutBoundTopic(topic, getUrlFormat(this.request));
    }

    @RequestMapping(path = "/mqtt_remove_outbound_topic", method = RequestMethod.GET)
    public boolean mqttRemoveOutBoundTopic(@RequestParam(name = "topic") String topic) throws BrokerException {
        checkSupport();
        return this.masterJob.getMqttTopic().mqttRemoveOutBoundTopic(topic, getUrlFormat(this.request));
    }
}
