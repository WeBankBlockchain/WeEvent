package com.webank.weevent.broker.fisco.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.ResponseData;
import com.webank.weevent.broker.fisco.service.BaseService;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.sdk.TopicInfo;

import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.abi.EventEncoder;
import org.bcos.web3j.abi.EventValues;
import org.bcos.web3j.abi.FunctionReturnDecoder;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Event;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.bcos.web3j.protocol.core.methods.request.EthFilter;
import org.bcos.web3j.protocol.core.methods.response.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Subscription;

/**
 * BlockChainServiceImpl Tester.
 *
 * @author websterchen
 * @version 1.0
 * @since 12/19/2018
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BrokerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BlockChainServiceImplTest extends BaseService {

    @Before
    public void before() throws Exception {
        loadConfig();
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testTransactionFilter() throws Exception {
        Subscription subscription = web3j.transactionObservable().subscribe(tx -> {
            System.out.println("tx:" + tx.getBlockNumber());
        });

        /*while (true) {
            sleep(1000);
        }*/
    }

    @Test
    public void testBlockFilter() throws Exception {
        Subscription subscription = web3j.blockObservable(false).subscribe(block -> {
            System.out.println("block:" + block.getResult().getNumber());
        });
        /*while (true) {
            sleep(1000);
        }*/
    }

    public static EventValues extractEventParameters(Event event, Log log) {
        List<String> topics = log.getTopics();
        String encodedEventSignature = EventEncoder.encode(event);
        if (!((String) topics.get(0)).equals(encodedEventSignature)) {
            return null;
        } else {
            List<Type> indexedValues = new ArrayList();
            List<Type> nonIndexedValues = FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());
            List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();

            for (int i = 0; i < indexedParameters.size(); ++i) {
                Type value = FunctionReturnDecoder.decodeIndexedValue((String) topics.get(i + 1), (TypeReference) indexedParameters.get(i));
                indexedValues.add(value);
            }

            return new EventValues(indexedValues, nonIndexedValues);
        }
    }

    public static int tranCount = 0;
    public static boolean flag = false;

    @Test
    public void testFilter() throws Exception {

        String topicName = "com.webank.test.websterchen";
        TopicServiceImpl topicService = new TopicServiceImpl();
        ResponseData<TopicInfo> topicInfoResponseData = topicService.getTopicInfo(topicName);
        TopicInfo topicInfo = topicInfoResponseData.getResult();
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                topicInfo.getTopicAddress());

        final Event event = new Event("LogWeEvent",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Utf8String>() {
                }));

        String topicData = EventEncoder.encode(event);
        filter.addSingleTopic(topicData);
        web3j.ethLogObservable(filter).subscribe(logs -> {
            EventValues eventValues = extractEventParameters(event, logs);
            String strTopicName = DataTypeUtils.bytes32ToString((Bytes32) eventValues.getNonIndexedValues().get(0));
            Integer weEventId = DataTypeUtils.uint256ToInt((Uint256) eventValues.getNonIndexedValues().get(1));
            Integer blockNumer = DataTypeUtils.uint256ToInt((Uint256) eventValues.getNonIndexedValues().get(2));
            Utf8String eventContent = (Utf8String) eventValues.getNonIndexedValues().get(3);
            if (flag == false) {
                tranCount = weEventId.intValue();
                tranCount = tranCount - 1;
                flag = true;
            }
            tranCount = tranCount + 1;
            System.out.println("console strTopicName:" + strTopicName + " weEventId:" + weEventId + " blockNumer:" + blockNumer + " eventContent:" + eventContent + " tranCount:" + tranCount);
            log.info("infolog strTopicName:{} weEventId:{} blockNumer:{} eventContent:{} tranCount:{}", strTopicName, weEventId, blockNumer, eventContent, tranCount);
        });

        /*while (true) {
            sleep(1000);
        }*/
    }

    /**
     * Method: getBlockNumber()
     */
    @Test
    public void testGetBlockNumber() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getNodeIdList()
     */
    @Test
    public void testGetNodeIdList() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getNodeIpList()
     */
    @Test
    public void testGetNodeIpList() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: main(String[] args)
     */
    @Test
    public void testMain() throws Exception {
//TODO: Test goes here... 
    }


}
