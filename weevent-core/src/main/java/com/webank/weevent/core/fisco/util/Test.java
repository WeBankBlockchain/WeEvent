package com.webank.weevent.core.fisco.util;

import java.io.File;
import java.io.IOException;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Test {

    public static void main(String[] args) throws IOException {
//        String configFile = Test.class.getClassLoader().getResource("fisco.toml").getPath();
//        BcosSDK sdk = BcosSDK.build(configFile);
//        ConfigOption config = sdk.getConfig();
//        sdk.getAmop();
        System.out.println("--------------");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        String aa = resolver.getClassLoader().getResource("classpath:" + "ca.crt").getPath();
        String s = resolver.getResource("classpath:" + "ca.crt").toString();

        File file1 = resolver.getResource("classpath:" + "ca.crt").getFile();
        boolean exists = file1.exists();
        File file2 = resolver.getResource("classpath:weevent-core\\ca.crt").getFile();
        boolean exists1 = file2.exists();
        System.out.println("--------------------");
        System.out.println(file1.exists());
        System.out.println(file2.exists());
    }
}
