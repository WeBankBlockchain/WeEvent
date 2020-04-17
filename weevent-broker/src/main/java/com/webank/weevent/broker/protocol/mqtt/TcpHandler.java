package com.webank.weevent.broker.protocol.mqtt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.client.BrokerException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * netty handler in MQTT.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
@Slf4j
public class TcpHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private ProtocolProcess protocolProcess;

    public TcpHandler(ProtocolProcess protocolProcess) {
        this.protocolProcess = protocolProcess;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) {
        String channelId = ctx.channel().id().asShortText();
        log.debug("MQTT message in, channel: {} {} {}", channelId, msg.fixedHeader(), msg.variableHeader());

        if (msg.decoderResult().isFailure()) {
            Optional<MqttMessage> rsp = this.protocolProcess.getDecodeFailureRsp(msg);
            rsp.ifPresent(mqttMessage -> sendRemote(ctx.channel(), mqttMessage));

            log.error("decode message failed, close channel");
            ctx.channel().close();
            return;
        }

        // process connect
        if (msg.fixedHeader().messageType() == MqttMessageType.CONNECT) {
            MqttConnectMessage connectMessage = (MqttConnectMessage) msg;
            MqttPublishMessage willMessage = this.protocolProcess.genWillMessage(connectMessage);
            SessionContext sessionData = new SessionContext(channelId,
                    connectMessage.payload().clientIdentifier(),
                    ctx.channel(),
                    connectMessage.variableHeader().isCleanSession(),
                    willMessage);

            MqttConnAckMessage rsp = this.protocolProcess.processConnect((MqttConnectMessage) msg, sessionData);
            sendRemote(ctx.channel(), rsp);
            if (rsp.variableHeader().connectReturnCode() != MqttConnectReturnCode.CONNECTION_ACCEPTED) {
                ctx.channel().close();
            }
            return;
        }

        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String remoteIp = socketAddress.getAddress().getHostAddress();

        // process the other commands
        try {
            Optional<MqttMessage> rsp = this.protocolProcess.process(msg, channelId, remoteIp);
            rsp.ifPresent(mqttMessage -> sendRemote(ctx.channel(), mqttMessage));

            // deal with heart beat from remote
            if (msg.fixedHeader().messageType() == MqttMessageType.CONNECT) {
                MqttConnectMessage connectMessage = (MqttConnectMessage) msg;
                int heartbeat = (int) (connectMessage.variableHeader().keepAliveTimeSeconds() * 1.5f);
                if (heartbeat > 0 && heartbeat < this.protocolProcess.getHeartBeat()) {
                    log.info("use heart beat from client, {}", connectMessage.variableHeader().keepAliveTimeSeconds());
                    ctx.channel().pipeline().replace(IdleStateHandler.class, "idle", new IdleStateHandler(0, 0, heartbeat));
                }
            }
        } catch (BrokerException e) {
            log.error("process MQTT command failed, close channel", e);
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            String channelId = ctx.channel().id().asShortText();
            log.error("close channel: {} for IOException: {}", channelId, cause.getMessage());
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
                log.info("channel idle too long, close channel: {}", channelId);
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asShortText();
        log.info("channel closed, channel: {}", channelId);
        this.protocolProcess.cleanSession(channelId);
    }

    public static void sendRemote(Channel channel, MqttMessage msg) {
        String channelId = channel.id().asShortText();
        log.info("send remote, channel: {} {}", channelId, msg.fixedHeader());
        channel.writeAndFlush(msg);
    }
}
