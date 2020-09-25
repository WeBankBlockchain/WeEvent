package com.webank.weevent.core.fisco;

import org.fisco.bcos.sdk.BcosSDK;
import org.junit.Test;

public class TestBcosSDK {

    @Test
    public void testBuild() {
        String configFile = TestBcosSDK.class.getClassLoader().getResource("fisco.toml").getPath();
        BcosSDK sdk = BcosSDK.build(configFile);
    }
}
