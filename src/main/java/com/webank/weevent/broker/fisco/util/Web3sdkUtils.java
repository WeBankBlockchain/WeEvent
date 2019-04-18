package com.webank.weevent.broker.fisco.util;


import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.contract.TopicController;
import com.webank.weevent.broker.fisco.contract.TopicData;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.bcos.channel.client.Service;
import org.bcos.contract.tools.ToolConf;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.crypto.GenCredential;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.bcos.web3j.tx.Contract;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utils 4 web3sdk.
 * depend on ApplicationContext which contains web3sdk configuration 'applicationContext.xml'.
 *
 * @author matthewliu
 * @since 2019/02/12
 */
@Slf4j
public class Web3sdkUtils {

    /**
     * init Web3j from context
     *
     * @param context ApplicationContext
     * @return Web3j return null if error
     */
    public static Web3j initWeb3j(ApplicationContext context) {
        log.info("begin init Web3j");

        Service service = context.getBean(Service.class);
        try {
            service.run();
        } catch (Exception e) {
            log.error("init Service failed", e);
            return null;
        }

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3j = Web3j.build(channelEthereumService);
        try {
            web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error("active Connections isEmpty,please check your credentials", e);
            return null;
        }

        log.info("init Web3j success");
        return web3j;
    }

    /**
     * init Credentials from context
     *
     * @param context ApplicationContext
     * @return Credentials return null if error
     */
    public static Credentials initCredentials(ApplicationContext context) {
        log.debug("begin init Credentials");

        ToolConf toolConf = context.getBean(ToolConf.class);
        Credentials credentials = GenCredential.create(toolConf.getPrivKey());
        if (null == credentials) {
            log.error("init Credentials failed");
            return null;
        }

        log.info("init Credentials success");
        return credentials;
    }

    /**
     * load contract handler
     *
     * @param contractAddress contractAddress
     * @param web3j web3j
     * @param credentials credentials
     * @param cls contract java class
     * @return Contract return null if error
     */
    public static Contract loadContract(String contractAddress, Web3j web3j, Credentials credentials, Class<?> cls) {
        log.info("begin load contract, {}", cls.getSimpleName());

        try {
            // load contract
            Method method = cls.getMethod(
                    "load",
                    String.class,
                    Web3j.class,
                    Credentials.class,
                    BigInteger.class,
                    BigInteger.class);

            Object contract = method.invoke(
                    null,
                    contractAddress,
                    web3j,
                    credentials,
                    WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT);

            if (contract == null) {
                log.info("load contract failed, {}", cls.getSimpleName());
                return null;
            } else {
                log.info("load contract success, {}", cls.getSimpleName());
                return (Contract) contract;
            }
        } catch (Exception e) {
            log.error("load contract failed, {} {}", cls.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * deploy topic control into web3j
     *
     * @param web3j web3j handler
     * @param credentials credentials
     * @return contract address
     * @throws BrokerException BrokerException
     */
    public static String deployTopicControl(Web3j web3j, Credentials credentials) throws BrokerException {
        log.info("begin deploy topic control");

        try {
            Future<TopicData> f1 = TopicData.deploy(web3j, credentials, WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT, WeEventConstants.INILITIAL_VALUE);
            TopicData topicData = f1.get(WeEventConstants.DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

            log.info("topic data contract address: {}", topicData.getContractAddress());
            if (topicData.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicData.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            Future<TopicController> f2 = TopicController.deploy(web3j, credentials, WeEventConstants.GAS_PRICE,
                    WeEventConstants.GAS_LIMIT, WeEventConstants.INILITIAL_VALUE, new Address(topicData.getContractAddress()));
            TopicController topicController = f2.get(WeEventConstants.DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

            log.info("topic control contract address: {}", topicController.getContractAddress());
            if (topicController.getContractAddress().equals(WeEventConstants.ADDRESS_EMPTY)) {
                log.error("contract address is empty after TopicController.deploy(...)");
                throw new BrokerException(ErrorCode.DEPLOY_CONTRACT_ERROR);
            }

            log.info("deploy topic control success");
            return topicController.getContractAddress();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("deploy contract failed", e);
            throw new BrokerException("deploy contract failed");
        }
    }

    private static void writeAddressToFile(String filePath, String contractName, String contractAddress) {
        try (FileWriter fileWritter = new FileWriter(filePath, false)) {
            String content = String.format("%s=%s", contractName, contractAddress);
            fileWritter.write(content);
            fileWritter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * tool to generate system contract, 'TopicController'.
     *
     * @param args optional param, save data into file if set.
     * 
     * Usage:
     *      java -Xbootclasspath/a:./config -cp weevent-broker-2.0.0.jar -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher [./address.txt]
     */
    public static void main(String[] args) {

        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            Web3j web3j = initWeb3j(context);
            Credentials credentials = initCredentials(context);

            String address = deployTopicControl(web3j, credentials);
            System.out.println("deploy contract[TopicController] success, address: " + address);

            if (args.length >= 1) {
                writeAddressToFile(args[0], "TopicController", address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // web3sdk can't exit gracefully
        System.exit(0);
    }
}
