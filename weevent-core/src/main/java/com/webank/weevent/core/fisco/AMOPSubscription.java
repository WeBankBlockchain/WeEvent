package com.webank.weevent.core.fisco;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.dto.AmopMsgResponse;
import com.webank.weevent.core.fisco.util.DataTypeUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.amop.Amop;
import org.fisco.bcos.sdk.amop.AmopCallback;
import org.fisco.bcos.sdk.amop.topic.AmopMsgIn;

/*
 * AMOP subscription
 *
 * @author matthewliu
 * @since 2020/06/02
 */
@Slf4j
@Getter
@Setter
@ToString
public class AMOPSubscription extends AmopCallback {
    public final static String SEPARATE = "-";
    /**
     * Binding groupId.
     */
    private final String groupId;
    private final Amop amop;

    // topic not verify
    public Map<String, IConsumer.ConsumerListener> subTopics = new ConcurrentHashMap<>();

    public AMOPSubscription(String groupId, Amop amop) {
        this.groupId = groupId;
        this.amop = amop;
    }

    public String addTopic(String topic, IConsumer.ConsumerListener listener) throws BrokerException {
        if (this.subTopics.containsKey(topic)) {
            log.error("exist topic, skip");
            throw new BrokerException(ErrorCode.TOPIC_ALREADY_EXIST);
        }

        this.subTopics.put(topic, listener);

        log.info("subscribe topic on AMOP channel, {}", topic);
        this.amop.subscribeTopic(topic, this);

        // subscriptionId
        return this.groupId + SEPARATE + topic;
    }

    public void removeTopic(String subscriptionId) throws BrokerException {
        String[] tokens = subscriptionId.split(SEPARATE);
        if (tokens.length != 2) {
            log.error("invalid subscriptionId");
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID);
        }

        String topic = tokens[1];
        if (!this.subTopics.containsKey(topic)) {
            log.error("not exist topic, skip");
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        this.subTopics.remove(topic);

        log.info("unSubscribe topic on AMOP channel, {}", topic);
        this.amop.unsubscribeTopic(topic);
    }

    @Override
    public byte[] receiveAmopMsg(AmopMsgIn amopMsgIn) {

        if (!this.subTopics.containsKey(amopMsgIn.getTopic())) {
            log.error("unknown topic on channel, {} -> {}", amopMsgIn.getTopic(), this.subTopics.keySet());
            return DataTypeUtils.toChannelResponse(ErrorCode.UNKNOWN_AMOP_SUB_TOPIC);
        }

        WeEvent event = null;
        try {
            event = JsonHelper.json2Object(amopMsgIn.getContent(), WeEvent.class);
        } catch (BrokerException e) {
            log.error("invalid WeEvent on channel", e);
            DataTypeUtils.toChannelResponse(ErrorCode.JSON_DECODE_EXCEPTION);
        }

        this.subTopics.get(amopMsgIn.getTopic()).onEvent(amopMsgIn.getTopic(), event);
        log.info("topic:{} receive amop message success, message:{}"
                , amopMsgIn.getTopic(), new String(amopMsgIn.getContent()));
        return DataTypeUtils.toChannelResponse(ErrorCode.SUCCESS, amopMsgIn.getContent());
    }
}
