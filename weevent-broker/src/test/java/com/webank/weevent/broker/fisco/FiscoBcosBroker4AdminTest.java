package com.webank.weevent.broker.fisco;


import java.math.BigInteger;
import java.util.List;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IAdmin;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FiscoBcosBroker4Admin Tester.
 *
 * @author puremilkfan
 * @version 1.1
 * @since 19/09/2019
 */
@Slf4j
public class FiscoBcosBroker4AdminTest extends JUnitTestBase {

    private IAdmin iAdmin;
    private QueryEntity queryEntity;
    private String groupId = WeEvent.DEFAULT_GROUP_ID;
    private final BigInteger blockNumber = BigInteger.valueOf(1);
    private final String blockHash = "0x361557fa0ee3c05e2e9bf629e8e9ba6d590d251ddba819f155cb9afc1c9cf588";
    private final String tranHash = "0xc74cfecef74e11f5143aa4c760f3f9804c84ee4c820b2b974b611517ae8887af";
    private final String nodeName = "1_60c08d803ac9b0a6c333ea12b1914ba3f8297c282088b79e987234076fd950ad78a864f04017f217056b95c8188c1ac32096a0f131f29cd607759a";
    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
        this.iAdmin = IAdmin.build();
    }

    /**
     * test getGroupGeneral
     */
    @Test
    public void testGetGroupGeneral() throws BrokerException {
        GroupGeneral groupGeneral = this.iAdmin.getGroupGeneral(this.groupId);
        Assert.assertNotNull(groupGeneral);
        Assert.assertNotNull(groupGeneral.getNodeCount());
        Assert.assertNotNull(groupGeneral.getLatestBlock());
        Assert.assertNotNull(groupGeneral.getTransactionCount());
    }

    /**
     * test queryTransList
     */
    @Test
    public void queryTransList() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        List<TbTransHash> tbTransHashes = this.iAdmin.queryTransList(queryEntity);
        Assert.assertNotNull(tbTransHashes);
        Assert.assertTrue(tbTransHashes.size() > 0);
    }


    /**
     * test blockNumber
     */
    @Test
    public void queryTransListBlockNumber() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setBlockNumber(blockNumber);
        List<TbTransHash> tbTransHashes = this.iAdmin.queryTransList(queryEntity);
        Assert.assertNotNull(tbTransHashes);
        Assert.assertTrue(tbTransHashes.size() > 0);
        Assert.assertEquals(tbTransHashes.get(0).getBlockNumber().toString(), this.blockNumber.toString());
    }


    /**
     * test transHash
     */
    @Test
    public void queryTransListTranHash() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setPkHash(tranHash);
        queryEntity.setGroupId(this.groupId);
        List<TbTransHash> tbTransHashes = this.iAdmin.queryTransList(queryEntity);
        Assert.assertNotNull(tbTransHashes);
        Assert.assertTrue(tbTransHashes.size() > 0);
        Assert.assertEquals(tbTransHashes.get(0).getTransHash(), this.tranHash);
    }


    @Test
    public void queryBlockList() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        List<TbBlock> tbBlocks = this.iAdmin.queryBlockList(queryEntity);
        Assert.assertNotNull(tbBlocks);
        Assert.assertTrue(tbBlocks.size() > 0);
    }

    @Test
    public void queryBlockListBlockNumber() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setBlockNumber(blockNumber);
        List<TbBlock> tbBlocks = this.iAdmin.queryBlockList(queryEntity);
        Assert.assertNotNull(tbBlocks);
        Assert.assertTrue(tbBlocks.size() > 0);
        Assert.assertEquals(tbBlocks.get(0).getBlockNumber().toString(), this.blockNumber.toString());
    }


    @Test
    public void queryBlockListBlockHash() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setPkHash(blockHash);
        List<TbBlock> tbBlocks = this.iAdmin.queryBlockList(queryEntity);
        Assert.assertNotNull(tbBlocks);
        Assert.assertTrue(tbBlocks.size() > 0);
        Assert.assertEquals(tbBlocks.get(0).getPkHash(), this.blockHash);
    }

    /**
     * test queryNodeList
     * @throws BrokerException
     */
    @Test
    public void queryNodeList() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        List<TbNode> tbNodes = this.iAdmin.queryNodeList(queryEntity);
        Assert.assertNotNull(tbNodes);
        Assert.assertTrue(tbNodes.size() > 0);
        Assert.assertEquals(tbNodes.get(0).getNodeName(),this.nodeName);
    }

}
