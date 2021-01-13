package com.webank.weevent.core.fisco;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.core.fisco.util.ParamCheckUtils;
import com.webank.weevent.core.fisco.util.Web3sdkUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class UtilsTest {
	
	private FiscoConfig fiscoConfig;
	
	@Autowired
	public void setFiscoConfig(FiscoConfig fiscoConfig) {
		this.fiscoConfig = fiscoConfig;
	}

    @Test
    public void testDeployContract() throws BrokerException {
        boolean deployRet = Web3sdkUtils.deployV2Contract(fiscoConfig);
        Assert.assertTrue(deployRet);
    }

    @Test
    public void testValidateFileName() {
        try {
            ParamCheckUtils.validateFileName(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateFileId() {
        try {
            ParamCheckUtils.validateFileId(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateFileId2() {
        try {
            String fileId = "test";
            ParamCheckUtils.validateFileId(fileId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateFileMd5() {
        try {
            ParamCheckUtils.validateFileMd5(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateChunkIdx() {
        try {
            ParamCheckUtils.validateChunkIdx(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateChunkData() {
        try {
            ParamCheckUtils.validateChunkData(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
