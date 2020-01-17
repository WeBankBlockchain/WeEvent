package com.webank.weevent.broker.fisco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.fisco.dto.ContractContext;
import com.webank.weevent.broker.fisco.util.RawTransactionUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.fisco.web3sdk.v2.solc10.Topic;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FiscoBcosBroker4Producer Tester.
 *
 * @author websterchen
 * @author matthewliu
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class FiscoBcosBroker4ProducerTest extends JUnitTestBase {
    private IProducer iProducer;
    private FiscoBcosDelegate fiscoBcosDelegate;
    private ContractContext contractContext;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.iProducer = BrokerApplication.applicationContext.getBean("iProducer", IProducer.class);
        this.fiscoBcosDelegate = BrokerApplication.applicationContext.getBean("fiscoBcosDelegate", FiscoBcosDelegate.class);
        this.contractContext = this.fiscoBcosDelegate.getContractContext(Long.parseLong(this.groupId));
        Assert.assertNotNull(this.iProducer);
        this.iProducer.startProducer();
        Assert.assertTrue(this.iProducer.open(this.topicName, this.groupId));
    }

    /**
     * Method: startProducer(String topic)
     */
    @Test
    public void testStartProducer() throws Exception {
        Assert.assertTrue(this.iProducer.startProducer());
    }

    /**
     * Method: shutdownProducer()
     */
    @Test
    public void testShutdownProducer() {
        Assert.assertTrue(this.iProducer.shutdownProducer());
    }

    /**
     * Method: publish(WeEvent event)
     */
    @Test
    public void testPublishEvent() throws Exception {
        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
    }

    /**
     * topic is exist and content is Chinese
     */
    @Test
    public void testPublishTopicExists() throws Exception {
        SendResult dto = this.iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes()), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
    }

    /**
     * test extensions is null
     */
    @Test
    public void testPublishExtIsNull() throws Exception {
        try {
            SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), null), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);
            Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode(), e.getCode());
        }
    }

    /**
     * extensions contain multiple key and value ,key start with 'weevent-'
     */
    @Test
    public void testPublishExtContainMulKey() throws Exception {
        Map<String, String> ext = new HashMap<>();
        ext.put("weevent-test", "test value");
        ext.put("weevent-test2", "test value2");
        ext.put("weevent-test3", "test value3");
        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * extensions contain multiple key and value ,one key not start with 'weevent-'
     */
    @Test
    public void testPublishExtContainMulKeyContainNotInvalidKey() {
        try {
            Map<String, String> ext = new HashMap<>();
            ext.put("weevent-test", "test value");
            ext.put("weevent-test2", "test value2");
            ext.put("test3", "test value3");
            this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_EXTENSIONS_KEY_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * extensions contain one key and value, key not start with 'weevent-'
     */
    @Test
    public void testPublishExtContainNotInvalidKey() {
        try {
            Map<String, String> ext = new HashMap<>();
            ext.put("test1", "test value");
            this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_EXTENSIONS_KEY_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * topic is not exists
     */
    @Test
    public void testPublishTopicNotExist() {
        try {
            String topicNotExists = "fsgdsggdgerer";
            this.iProducer.publish(new WeEvent(topicNotExists, "中文消息.".getBytes()), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * topic is blank
     */
    @Test
    public void testPublishTopicIsBlank() {
        try {
            this.iProducer.publish(new WeEvent(" ", "中文消息.".getBytes()), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testPublishTopicIsNull() {
        try {
            this.iProducer.publish(new WeEvent(null, "中文消息.".getBytes()), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testPublishTopicOverMaxLen() {
        try {
            String topicNotExists = "fsgdsggdgererqwertyuioplkjhgfdsazxqazwsxedcrfvtgbyhnujmikolppoiuyt";
            this.iProducer.publish(new WeEvent(topicNotExists, "中文消息.".getBytes()), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic is exits and content is null
     */
    @Test
    public void testPublishContentIsNull() {
        try {
            this.iProducer.publish(new WeEvent(this.topicName, null), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_CONTENT_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic is exits and content is blank
     */
    @Test
    public void testPublishContentIsBlank() {
        try {
            byte[] bytes = "".getBytes();
            this.iProducer.publish(new WeEvent(this.topicName, bytes), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_CONTENT_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * groupId is null
     */
    @Test
    public void testPublishGroupIdIsNull() throws Exception {
        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes()), null).get(transactionTimeout, TimeUnit.MILLISECONDS);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * groupId is not exist
     */
    @Test
    public void testPublishGroupIdIsNotExist() {
        try {
            this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes()), "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not number
     */
    @Test
    public void testPublishGroupIdIsNotNum() {
        try {
            this.iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes()), "sfsdf");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special character without in [32,128]
     */
    @Test
    public void testPublishTopicContainSpecialChar() {
        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            byte[] bytes = "hello world".getBytes();
            this.iProducer.publish(new WeEvent(illegalTopic, bytes), this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic is Chinese character
     */
    @Test
    public void testPublishTopicContainChineseChar() {
        try {
            byte[] bytes = "".getBytes();
            this.iProducer.publish(new WeEvent("中国", bytes), this.groupId);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * publish with custom header
     */
    @Test
    public void testPublishTag() throws Exception {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, "create");
        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, "hello tag: create".getBytes(), ext), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * publish an externally signed event by fixed account.
     */
    @Test
    public void testPublishByFixedAccount() throws Exception {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_SIGN, "true");
        WeEvent event = new WeEvent(this.topicName, "this is a signed message".getBytes(), ext);
        ContractContext contractContext = this.fiscoBcosDelegate.getContractContext(Long.parseLong(this.groupId));

        String rawData = RawTransactionUtils.buildWeEvent(event);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, contractContext);


        String signData = RawTransactionUtils.signData(rawTransaction, RawTransactionUtils.getFixedAccountCredentials());
        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, signData.getBytes(), ext), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * publish an externally signed event by external account.
     */
    @Test
    public void testPublishByExternalAccount() throws Exception {
        Credentials fixedCredentials = RawTransactionUtils.getFixedAccountCredentials();
        Credentials externallyCredentials = RawTransactionUtils.getExternalAccountCredentials();
        String operatorAddress = externallyCredentials.getAddress();

        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_ADDOPERATOR, operatorAddress);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, fixedCredentials);
        // add operatorAddress for topic
        boolean result = this.iProducer.addOperator(this.groupId, this.topicName, signData);
        Assert.assertTrue(result);

        // publish event with the above generated externally account
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_SIGN, "true");
        WeEvent event = new WeEvent(this.topicName, "this is a signed message".getBytes(), ext);

        rawData = RawTransactionUtils.buildWeEvent(event);
        rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        signData = RawTransactionUtils.signData(rawTransaction, externallyCredentials);

        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, signData.getBytes(), ext), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * publish an externally signed event by external account.
     */
    @Test
    public void testPublishByExternalAccountNoPermission() throws Exception {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_SIGN, "true");
        WeEvent event = new WeEvent(this.topicName, "this is a signed message".getBytes(), ext);

        String rawData = RawTransactionUtils.buildWeEvent(event);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        Credentials credentials = RawTransactionUtils.getExternalAccountCredentials();
        System.out.println(credentials.getAddress());

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);
        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, signData.getBytes(), ext), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.NO_PERMISSION);
    }

    /**
     * check exist operator.
     */
    @Test
    public void testCheckExistOperatorPermission() throws Exception {
        // fixed account
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_CHECKOPERATORPERMISSION, "");
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        boolean result = this.iProducer.checkOperatorPermission(this.groupId, this.topicName, signData);
        Assert.assertTrue(result);

    }

    /**
     * check not exist operator.
     */
    @Test
    public void testCheckNotExistOperatorPermission() throws Exception {
        // fixed account
        Credentials credentials = RawTransactionUtils.getExternalAccountCredentials();

        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_CHECKOPERATORPERMISSION, "");
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        boolean result = this.iProducer.checkOperatorPermission(this.groupId, this.topicName, signData);
        Assert.assertFalse(result);

    }

    /**
     * add operator by fixed account.
     */
    @Test
    public void testAddOperator() throws BrokerException {
        // fixed account
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        // new address
        String address = RawTransactionUtils.getExternalAccountCredentials().getAddress();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_ADDOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        boolean result = this.iProducer.addOperator(this.groupId, this.topicName, signData);
        Assert.assertTrue(result);

    }

    /**
     * add operator by fixed account, topic not exist.
     */
    @Test
    public void testAddOperatorTopicNotExist() {
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        // new address
        String address = RawTransactionUtils.getExternalAccountCredentials().getAddress();
        String rawData = RawTransactionUtils.buildACLData("AAA", Topic.FUNC_ADDOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        try {
            // operator already exist
            this.iProducer.addOperator(this.groupId, "AAA", signData);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * add exist operator by fixed account.
     */
    @Test
    public void testAddOperatorAlreadyExist() {
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        // new address
        String address = RawTransactionUtils.getExternalAccountCredentials().getAddress();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_ADDOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        try {
            boolean result = this.iProducer.addOperator(this.groupId, this.topicName, signData);
            Assert.assertTrue(result);
            // operator already exist
            this.iProducer.addOperator(this.groupId, this.topicName, signData);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OPERATOR_ALREADY_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * add operator by external account, no permission.
     */
    @Test
    public void testAddOperatorNoPermission() {
        // external account
        Credentials credentials = RawTransactionUtils.getExternalAccountCredentials();
        String address = credentials.getAddress();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_ADDOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        try {
            this.iProducer.addOperator(this.groupId, this.topicName, signData);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.NO_PERMISSION.getCode(), e.getCode());
        }
    }

    /**
     * add operator by fixed account.
     */
    @Test
    public void testDelOperator() throws BrokerException {
        // fixed account
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        // new address
        String address = RawTransactionUtils.getExternalAccountCredentials().getAddress();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_ADDOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);
        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        // add operator
        boolean addResult = this.iProducer.addOperator(this.groupId, this.topicName, signData);
        Assert.assertTrue(addResult);

        rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_DELOPERATOR, address);
        rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);
        signData = RawTransactionUtils.signData(rawTransaction, credentials);
        // delete operator
        boolean delResult = this.iProducer.delOperator(this.groupId, this.topicName, signData);
        Assert.assertTrue(delResult);
    }

    /**
     * delete operator by fixed account, topic not exist.
     */
    @Test
    public void testDelOperatorTopicNotExist() {
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        // new address
        String address = RawTransactionUtils.getExternalAccountCredentials().getAddress();
        String rawData = RawTransactionUtils.buildACLData("AAA", Topic.FUNC_DELOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        try {
            this.iProducer.delOperator(this.groupId, "AAA", signData);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * delete not exist operator by fixed account.
     */
    @Test
    public void testDelOperatorNotExist() {
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        // new address
        String address = RawTransactionUtils.getExternalAccountCredentials().getAddress();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_DELOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        try {
            this.iProducer.delOperator(this.groupId, this.topicName, signData);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OPERATOR_NOT_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * delete operator by external account, no permission.
     */
    @Test
    public void testDelOperatorNoPermission() {
        // external account
        Credentials credentials = RawTransactionUtils.getExternalAccountCredentials();
        String address = credentials.getAddress();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_DELOPERATOR, address);
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        try {
            this.iProducer.delOperator(this.groupId, this.topicName, signData);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.NO_PERMISSION.getCode(), e.getCode());
        }
    }

    /**
     * get operator list by fixed account.
     */
    @Test
    public void testGetOperatorList() throws BrokerException {
        // fixed account
        Credentials credentials = RawTransactionUtils.getFixedAccountCredentials();
        String rawData = RawTransactionUtils.buildACLData(this.topicName, Topic.FUNC_LISTOPERATOR, "");
        ExtendedRawTransaction rawTransaction = RawTransactionUtils.getRawTransaction(this.groupId, rawData, this.contractContext);

        String signData = RawTransactionUtils.signData(rawTransaction, credentials);

        List<String> operatorList = this.iProducer.listOperator(this.groupId, this.topicName, signData);
        Assert.assertTrue(operatorList.size() >= 1);
    }

    /**
     * transactionHex null.
     */
    @Test
    public void testTransactionHexNull() {
        try {
            this.iProducer.listOperator(this.groupId, this.topicName, "");
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TRANSACTIONHEX_IS_NULL.getCode(), e.getCode());
        }
    }

    /**
     * transactionHex illegal.
     */
    @Test
    public void testTransactionHexIllegal() {
        try {
            this.iProducer.listOperator(this.groupId, this.topicName, "asdfghjkl");
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TRANSACTIONHEX_ILLEGAL.getCode(), e.getCode());
        }
    }
}
