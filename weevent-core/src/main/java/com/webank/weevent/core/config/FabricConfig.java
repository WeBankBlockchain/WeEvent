package com.webank.weevent.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.webank.weevent.core.fisco.util.WeEventUtils;

import lombok.Data;
import lombok.ToString;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/9
 */
@Data
@ToString
@Component
@PropertySource(value = "classpath:fabric/fabric.properties", ignoreResourceNotFound = true, encoding = "UTF-8")
public class FabricConfig {
	
    @Value("${chain.channel.name:mychannel}")
    private String channelName;

    @Value("${chain.organizations.name}")
    private String orgName;

    @Value("${chain.organizations.mspid}")
    private String mspId;

    @Value("${chain.organizations.username}")
    private String orgUserName;

    @Value("${chain.organizations.user.keyfile:}")
    private String orgUserKeyFile;

    @Value("${chain.organizations.user.certfile:}")
    private String orgUserCertFile;

    @Value("${chain.peer.address:}")
    private String peerAddress;

    @Value("${chain.peer.tls.cafile:}")
    private String peerTlsCaFile;

    @Value("${chain.orderer.address:}")
    private String ordererAddress;

    @Value("${chain.orderer.tls.cafile:}")
    private String ordererTlsCaFile;

    @Value("${chaincode.topic.version}")
    private String topicVerison;

    @Value("${chaincode.topic.name}")
    private String topicName;

    private String topicSourceLoc;

    @Value("${chaincode.topic.path}")
    private String topicPath;

    @Value("${chaincode.topic-controller.version}")
    private String topicControllerVersion;

    @Value("${chaincode.topic-controller.name}")
    private String topicControllerName;

    private String topicControllerSourceLoc;

    @Value("${chaincode.topic-controller.path}")
    private String topicControllerPath;

    @Value("${chaincode.proposal.timeout}")
    private Long proposalTimeout;

    @Value("${chaincode.transaction.timeout}")
    private Long transactionTimeout;

    @Value("${pool.core-pool-size}")
    private Integer corePoolSize;

    @Value("${pool.max-pool-size}")
    private Integer maxPoolSize;

    @Value("${pool.keep-alive-seconds}")
    private Integer keepAliveSeconds;

    @Value("${consumer.idle-time}")
    private Integer consumerIdleTime;

    @Value("${consumer.history_merge_block}")
    private Integer consumerHistoryMergeBlock;
    
    /**
     * load configuration without spring
     *
     * @param configFile config file, if empty load from default location
     * @return true if success, else false
     */
    public boolean load(String configFile) {
        boolean loadResult = new SmartLoadConfig().load(this, configFile, "");
        this.setOrgUserKeyFile(WeEventUtils.getClassPath() + this.getOrgUserKeyFile());
        this.setOrgUserCertFile(WeEventUtils.getClassPath() + this.getOrgUserCertFile());
        this.setOrdererTlsCaFile(WeEventUtils.getClassPath() + this.getOrdererTlsCaFile());
        this.setPeerTlsCaFile(WeEventUtils.getClassPath() + this.getPeerTlsCaFile());

        this.setTopicSourceLoc(WeEventUtils.getClassPath() + "fabric");
        this.setTopicControllerSourceLoc(WeEventUtils.getClassPath() + "fabric");

        return loadResult;
    }
    
}
