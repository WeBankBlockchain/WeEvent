package com.webank.weevent.broker.protocol.mqtt;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * netty handler in MQTT.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
@Slf4j
public class BrokerHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private ProtocolProcess protocolProcess;

    // channel id <-> clientId
    private Map<String, String> authorChannels = new ConcurrentHashMap<>();

    public BrokerHandler(ProtocolProcess protocolProcess) {
        this.protocolProcess = protocolProcess;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        log.debug("MQTT message in, channelId: {} {} {}", channelId, msg.fixedHeader(), msg.variableHeader());

        // CONNECT is difficult
        if (msg.fixedHeader().messageType() == MqttMessageType.CONNECT) {
            if (this.authorChannels.containsKey(channelId)) {
                log.error("MUST be CONNECT only once");
                ctx.close();
                return;
            }

            String clientId = protocolProcess.getConnect().processConnect(ctx.channel(), (MqttConnectMessage) msg);
            if (StringUtils.isEmpty(clientId)) {
                ctx.close();
                return;
            }

            log.info("MQTT connected, channelId: {} clientId: {}", channelId, clientId);
            this.authorChannels.put(channelId, clientId);
            return;
        }

        if (!this.authorChannels.containsKey(channelId)) {
            log.error("MUST CONNECT first, close");
            ctx.close();
            return;
        }

        String clientId = this.authorChannels.get(channelId);
        if (!this.protocolProcess.getSessionStore().getSession(clientId).isPresent()) {
            log.error("unknown clientId, close");
            ctx.close();
            return;
        }

        switch (msg.fixedHeader().messageType()) {
            case PINGREQ:
                this.protocolProcess.getPingReq().processPingReq(ctx.channel(), msg);
                break;

            case PUBLISH:
                this.protocolProcess.getPublish().processPublish(ctx.channel(), (MqttPublishMessage) msg);
                break;

            case PUBACK:
                this.protocolProcess.getPubAck().processPubAck(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;

            case SUBSCRIBE:
                this.protocolProcess.getSubscribe().processSubscribe(ctx.channel(), clientId, (MqttSubscribeMessage) msg);
                break;

            case UNSUBSCRIBE:
                this.protocolProcess.getUnSubscribe().processUnSubscribe(ctx.channel(), clientId, (MqttUnsubscribeMessage) msg);
                break;

            case DISCONNECT:
                this.protocolProcess.getDisConnect().processDisConnect(ctx.channel(), clientId, msg);
                break;

            default:
                log.error("unSupport command, {}", msg.fixedHeader().messageType());
                ctx.close();
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            String channelId = ctx.channel().id().asShortText();
            log.error("IOException on channel, close channelId: {}", channelId);
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                String channelId = ctx.channel().id().asShortText();
                log.info("channel idle too long, close channelId: {}", channelId);
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asShortText();
        log.info("channel closed, channelId: {}", channelId);
        this.cleanChannel(channelId);
    }

    private void cleanChannel(String channelId) {
        log.info("clean channel: {}", channelId);

        if (this.authorChannels.containsKey(channelId)) {
            String clientId = this.authorChannels.get(channelId);
            this.protocolProcess.cleanSession(clientId);
            this.authorChannels.remove(channelId);
        }
    }

    public static void sendRemote(Channel channel, MqttMessage msg) {
        String channelId = channel.id().asShortText();
        log.info("send remote, channelId: {} {}", channelId, msg.fixedHeader());
        channel.writeAndFlush(msg);
    }
}
