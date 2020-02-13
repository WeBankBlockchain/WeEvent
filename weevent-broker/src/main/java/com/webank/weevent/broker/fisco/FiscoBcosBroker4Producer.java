package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FiscoBcosBroker4Producer extends FiscoBcosTopicAdmin implements IProducer {

    public FiscoBcosBroker4Producer(FiscoBcosDelegate fiscoBcosDelegate) {
        super(fiscoBcosDelegate);
    }

    @Override
    public boolean startProducer() {
        return true;
    }

    @Override
    public boolean shutdownProducer() {
        return true;
    }

    @Override
    public CompletableFuture<SendResult> publish(WeEvent event, String groupIdStr) throws BrokerException {
        log.debug("publish {} groupId: {}", event, groupIdStr);

        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateEvent(event);

        // publishEvent support async operator in callback
        if (event.getExtensions() != null && event.getExtensions().containsKey(WeEvent.WeEvent_SIGN)) {
            return fiscoBcosDelegate.sendRawTransaction(event.getTopic(),
                    Long.parseLong(groupId),
                    new String(event.getContent(), StandardCharsets.UTF_8));
        } else {
            return fiscoBcosDelegate.publishEvent(event.getTopic(),
                    Long.parseLong(groupId),
                    new String(event.getContent(), StandardCharsets.UTF_8),
                    DataTypeUtils.object2Json(event.getExtensions()));
        }
    }

    @Override
    public byte[] downloadChunk(String groupId, String fileId, int chunkIdx) {
        return new byte[0];
    }

    @Override
    public FileChunksMeta listChunk(String groupIdStr, String fileId) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        return fiscoBcosDelegate.listChunk(Long.valueOf(groupId), fileId);
    }

    @Override
    public FileChunksMeta createChunk(String groupIdStr, long fileSize, String md5) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);

        return fiscoBcosDelegate.createChunk(Long.valueOf(groupId), fileSize, md5);
    }

    @Override
    public FileChunksMeta uploadChunk(String groupIdStr, String fileId, int chunkIdx, byte[] chunkData) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);

        // fileName?
        return fiscoBcosDelegate.upload(Long.valueOf(groupId), fileId, "", chunkData, chunkIdx);
    }
}
