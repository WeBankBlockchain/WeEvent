package weevent.robust.service.interfaces;


import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttInputChannel")
public interface MqttSubscribe {
    void subscribeToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);

}
