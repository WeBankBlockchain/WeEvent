package com.webank.weevent.ST.plugin;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.fisco.service.impl.BlockChainServiceImpl;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class DeployTest extends JUnitTestBase {

    @Test
    public void testDeploy() throws BrokerException {
        BlockChainServiceImpl block = new BlockChainServiceImpl();
        String address = block.deployTopicContracts();
        System.out.println(address);
    }

}
