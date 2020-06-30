package com.webank.weevent.broker.st;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.webank.weevent.broker.JUnitTestBase;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class MQTTTest extends JUnitTestBase {
    private final String topicName = "com.weevent.test";

    private final String url = "tcp://localhost:7001";
    private final int actionTimeout = 3000;

    private MqttClient mqttClient;
    private final String content = "hello mqtt via tcp";

    private MqttConnectOptions cleanupOptions;
    private MqttConnectOptions persistOptions;

    static class MessageListener implements IMqttMessageListener {
        public int received = 0;

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            log.info("received message, {}", message.getPayload());
            received++;
        }
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        String clientId = UUID.randomUUID().toString();

        this.cleanupOptions = new MqttConnectOptions();
        this.cleanupOptions.setConnectionTimeout(this.actionTimeout);
        this.cleanupOptions.setKeepAliveInterval(this.actionTimeout);
        this.cleanupOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        this.cleanupOptions.setCleanSession(true);

        this.persistOptions = new MqttConnectOptions();
        this.persistOptions.setConnectionTimeout(this.actionTimeout);
        this.persistOptions.setKeepAliveInterval(this.actionTimeout);
        this.persistOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        this.persistOptions.setCleanSession(false);

        this.mqttClient = new MqttClient(this.url, clientId, null);
        this.mqttClient.connect(this.cleanupOptions);
    }

    @After
    public void after() throws Exception {
        if (this.mqttClient != null && this.mqttClient.isConnected()) {
            this.mqttClient.disconnect();
        }
    }

    @Test
    public void testConnectWithDefaultVersion() {
        try {
            String clientId = UUID.randomUUID().toString();
            MqttClient mqttClient = new MqttClient(this.url, clientId, null);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setConnectionTimeout(this.actionTimeout);
            mqttClient.connect(connOpts);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testConnect31() {
        try {
            // client id must less then 23 bytes in 3.1
            String clientId = UUID.randomUUID().toString().split("-")[0];
            MqttClient mqttClient = new MqttClient(this.url, clientId, null);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setConnectionTimeout(this.actionTimeout);
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            mqttClient.connect(connOpts);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test(expected = MqttException.class)
    public void testConnect31ClientTooLong() throws MqttException {
        // client id must less then 23 bytes in 3.1
        String clientId = UUID.randomUUID().toString();
        MqttClient mqttClient = new MqttClient(this.url, clientId, null);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setConnectionTimeout(this.actionTimeout);
        connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        mqttClient.connect(connOpts);

        Assert.assertTrue(true);
    }

    @Test
    public void testConnect2Times() {
        String clientId = UUID.randomUUID().toString();

        try {
            MqttClient mqttClient = new MqttClient(this.url, clientId, null);
            mqttClient.connect(this.cleanupOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }

        try {
            MqttClient mqttClient2 = new MqttClient(this.url, clientId, null);
            mqttClient2.connect(this.cleanupOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testWill() {
        try {
            String clientId = UUID.randomUUID().toString();
            MqttClient mqttClient = new MqttClient(this.url, clientId, null);

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setConnectionTimeout(this.actionTimeout);
            connectOptions.setKeepAliveInterval(this.actionTimeout);
            connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            connectOptions.setWill(this.topicName, this.content.getBytes(), 1, false);
            connectOptions.setCleanSession(true);
            mqttClient.connect(this.cleanupOptions);
            mqttClient.disconnect();

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testPing() throws InterruptedException {
        try {
            Thread.sleep((long) this.actionTimeout * 3);

            Assert.assertTrue(true);
        } catch (InterruptedException e) {
            log.error("exception", e);
            Assert.fail();
            throw e;
        }
    }

    @Test
    public void testPublishQos0() {
        try {
            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            message.setQos(0);
            this.mqttClient.publish(this.topicName, message);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testPublishQos1() {
        try {
            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            this.mqttClient.publish(this.topicName, message);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test(expected = MqttException.class)
    public void testPublishQos2() throws MqttException {
        MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
        message.setQos(2);
        this.mqttClient.publish(this.topicName, message);

        Assert.assertTrue(true);
    }

    @Test
    public void testPublish2Times() {
        try {
            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            this.mqttClient.publish(this.topicName, message);
            this.mqttClient.publish(this.topicName, message);

            Assert.assertTrue(true);
        } catch (Exception e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testSubscribe() {
        try {
            MessageListener listener = new MessageListener();
            IMqttToken token = this.mqttClient.subscribeWithResponse(this.topicName, listener);
            token.waitForCompletion();

            Assert.assertEquals(token.getGrantedQos()[0], MqttQoS.AT_LEAST_ONCE.value());

            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            this.mqttClient.publish(this.topicName, message);

            Thread.sleep(this.actionTimeout);
            Assert.assertTrue(listener.received > 0);
        } catch (MqttException | InterruptedException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testSubscribeNotExist() {
        try {
            MessageListener listener = new MessageListener();
            IMqttToken token = this.mqttClient.subscribeWithResponse("not_exist", listener);
            token.waitForCompletion();

            Assert.assertNotEquals(token.getGrantedQos()[0], MqttQoS.AT_LEAST_ONCE.value());
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testSubscribeWildcard() {
        try {
            MessageListener listener = new MessageListener();
            IMqttToken token = this.mqttClient.subscribeWithResponse("#", listener);
            token.waitForCompletion();

            Assert.assertEquals(token.getGrantedQos()[0], MqttQoS.AT_LEAST_ONCE.value());

            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            this.mqttClient.publish(this.topicName, message);

            Thread.sleep(this.actionTimeout);
            Assert.assertTrue(listener.received > 0);
        } catch (Exception e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testSubscribeBatch() {
        try {
            MessageListener listener = new MessageListener();
            String[] topics = {this.topicName, "not_exist"};
            IMqttToken token = this.mqttClient.subscribeWithResponse(topics, new IMqttMessageListener[]{listener, listener});
            token.waitForCompletion();

            // do right
            Assert.assertEquals(token.getGrantedQos()[0], MqttQoS.AT_LEAST_ONCE.value());
            // not exist
            Assert.assertNotEquals(token.getGrantedQos()[1], MqttQoS.AT_LEAST_ONCE.value());

            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            this.mqttClient.publish(this.topicName, message);

            Thread.sleep(this.actionTimeout);
            Assert.assertTrue(listener.received > 0);
        } catch (Exception e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testSubscribeQos0() {
        try {
            MessageListener listener = new MessageListener();
            this.mqttClient.subscribeWithResponse(this.topicName, listener).waitForCompletion();

            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            this.mqttClient.publish(this.topicName, message);

            Thread.sleep(this.actionTimeout);
            Assert.assertTrue(listener.received > 0);
        } catch (Exception e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test(expected = MqttException.class)
    public void testSubscribeQos2() throws MqttException {
        MessageListener listener = new MessageListener();
        this.mqttClient.subscribeWithResponse(this.topicName, 2, listener).waitForCompletion();

        Assert.assertTrue(true);
    }

    @Test
    public void testPersistConnect() {
        try {
            String clientId = UUID.randomUUID().toString();
            MqttClient client = new MqttClient(this.url, clientId, null);
            client.connect(this.persistOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testPersistConnectAgain() {
        try {
            String clientId = UUID.randomUUID().toString();
            MqttClient client = new MqttClient(this.url, clientId, null);
            client.connect(this.persistOptions);
            client.disconnect();
            client.connect(this.persistOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testPersistConnect2Times() {
        String clientId = UUID.randomUUID().toString();
        try {
            MqttClient client1 = new MqttClient(this.url, clientId, null);
            client1.connect(this.persistOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }

        try {
            MqttClient client2 = new MqttClient(this.url, clientId, null);
            client2.connect(this.persistOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testCleanSession() {
        String clientId = UUID.randomUUID().toString();
        try {
            MqttClient client1 = new MqttClient(this.url, clientId, null);
            client1.connect(this.persistOptions);

            // subscription
            MessageListener listener = new MessageListener();
            client1.subscribeWithResponse(this.topicName, listener).waitForCompletion();

            client1.disconnect();

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }

        // clean session with clean flag
        try {
            MqttClient client2 = new MqttClient(this.url, clientId, null);
            client2.connect(this.cleanupOptions);

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testPersistSessionState() {
        String clientId = UUID.randomUUID().toString();
        try {
            MqttClient client1 = new MqttClient(this.url, clientId, null);
            client1.connect(this.persistOptions);

            // subscription
            MessageListener listener = new MessageListener();
            client1.subscribeWithResponse(this.topicName, listener).waitForCompletion();

            client1.disconnect();
            client1.close();
            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }

        // publish after clientId disconnect
        try {
            MqttMessage message = new MqttMessage(this.content.getBytes(StandardCharsets.UTF_8));
            this.mqttClient.publish(this.topicName, message);
        } catch (MqttException e) {
            log.error("publish failed", e);
            Assert.fail();
        }

        // reconnect subscribe get the offline message
        try {
            Thread.sleep(this.actionTimeout);

            MqttClient client2 = new MqttClient(this.url, clientId, null);
            client2.connect(this.persistOptions);

            // subscription
            MessageListener listener = new MessageListener();
            client2.subscribeWithResponse(this.topicName, listener).waitForCompletion();

            Thread.sleep(this.actionTimeout);
            // received lost message
            Assert.assertTrue(listener.received > 0);
        } catch (MqttException | InterruptedException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testDisconnect() {
        try {
            this.mqttClient.disconnect();
            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }

    @Test
    public void testClose() {
        try {
            this.mqttClient.disconnect();
            this.mqttClient.close();

            Assert.assertTrue(true);
        } catch (MqttException e) {
            log.error("exception", e);
            Assert.fail();
        }
    }
}
