package com.webank.weevent.protocol.jsonrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implement of JsonRpc service.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
@Slf4j
@JsonRpcService("/jsonrpc")
@AutoJsonRpcServiceImpl
@Component
public class BrokerRpc implements IBrokerRpc {
    private IProducer producer;

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    private SendResult publishInner(WeEvent event, String groupId) throws BrokerException {
        try {
            return this.producer.publish(event, groupId).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("publishWeEvent failed due to transaction execution error.", e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (TimeoutException e) {
            log.error("publishWeEvent failed due to transaction execution timeout.", e);
            SendResult sendResult = new SendResult();
            sendResult.setTopic(event.getTopic());
            sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
            return sendResult;
        }
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "content") byte[] content) throws BrokerException {
        log.info("topic:{} content.length:{}", topic, content.length);

        return this.publishInner(new WeEvent(topic, content, new HashMap<>()), "");
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "groupId") String groupId,
                              @JsonRpcParam(value = "content") byte[] content,
                              @JsonRpcParam(value = "extensions") Map<String, String> extensions) throws BrokerException {
        log.info("topic:{} groupId:{} content.length:{} extensions:{}", topic, groupId, content.length, DataTypeUtils.object2Json(extensions));

        return this.publishInner(new WeEvent(topic, content, extensions), groupId);
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId,
                            @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("eventId:{} groupId:{}", eventId, groupId);

        return this.producer.getEvent(eventId, groupId);
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId) throws BrokerException {
        log.info("eventId:{}", eventId);

        return this.producer.getEvent(eventId, "");
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic,
                        @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.open(topic, groupId);
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("topic:{}", topic);

        return this.producer.open(topic, "");
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.close(topic, groupId);
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("topic:{}", topic);

        return this.producer.close(topic, "");
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.exist(topic, groupId);
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("topic:{} groupId", topic);

        return this.producer.exist(topic, "");
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize,
                          @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("pageIndex:{} pageSize:{} groupId:{}", pageIndex, pageSize, groupId);

        return this.producer.list(pageIndex, pageSize, groupId);
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize) throws BrokerException {
        log.info("pageIndex:{} pageSize:{}", pageIndex, pageSize);

        return this.producer.list(pageIndex, pageSize, "");
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic,
                           @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.state(topic, groupId);
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("topic:{}", topic);

        return this.producer.state(topic, "");
    }
}
