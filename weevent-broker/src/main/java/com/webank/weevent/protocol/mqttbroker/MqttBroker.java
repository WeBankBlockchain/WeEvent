package com.webank.weevent.protocol.mqttbroker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.protocol.mqttbroker.common.MqttWebSocketCodec;
import com.webank.weevent.protocol.mqttbroker.handler.BrokerHandler;
import com.webank.weevent.protocol.mqttbroker.mqttprotocol.ProtocolProcess;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *@ClassName BrokerServer
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/20 10:17
 *@Version 1.0
 **/
@Component
@Slf4j
public class MqttBroker {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel websocketChannel;
    private Channel mqttChannel;

    @Autowired
    private ProtocolProcess protocolProcess;

    @PostConstruct
    public void start() throws Exception {
        if (BrokerApplication.weEventConfig.getBrokerServerPort() != null
                || BrokerApplication.weEventConfig.getWebSocketPort() != null) {

            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();

            // tcp
            if (BrokerApplication.weEventConfig.getBrokerServerPort() != null) {
                this.mqttChannel = mqttServer();
            }

            // websocket
            if (BrokerApplication.weEventConfig.getWebSocketPort() != null) {
                this.websocketChannel = webSocketServer();
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
            this.bossGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }

        if (this.mqttChannel != null) {
            this.mqttChannel.closeFuture().syncUninterruptibly();
            this.mqttChannel = null;
        }
        if (this.websocketChannel != null) {
            this.websocketChannel.closeFuture().syncUninterruptibly();
            this.websocketChannel = null;
        }
    }

    private Channel mqttServer() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        channelPipeline.addFirst("idle", new IdleStateHandler(
                                0,
                                0,
                                BrokerApplication.weEventConfig.getKeepAlive()));

                        //channelPipeline.addLast("ssl", getSslHandler(sslContext, socketChannel.alloc()));
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, BrokerApplication.weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, BrokerApplication.weEventConfig.getSoKeepAlive());
        return serverBootstrap.bind(BrokerApplication.weEventConfig.getBrokerServerPort()).sync().channel();
    }

    private Channel webSocketServer() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        channelPipeline.addFirst("idle", new IdleStateHandler(0,
                                0,
                                BrokerApplication.weEventConfig.getKeepAlive()));
                        //channelPipeline.addLast("ssl", getSslHandler(sslContext, socketChannel.alloc()));
                        channelPipeline.addLast("http-codec", new HttpServerCodec());
                        channelPipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                        channelPipeline.addLast("compressor ", new HttpContentCompressor());
                        channelPipeline.addLast("protocol", new WebSocketServerProtocolHandler(
                                BrokerApplication.weEventConfig.getWebSocketServerPath(),
                                "mqtt,mqttv3.1,mqttv3.1.1",
                                true,
                                65536));
                        channelPipeline.addLast("mqttWebSocket", new MqttWebSocketCodec());
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, BrokerApplication.weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, BrokerApplication.weEventConfig.getSoKeepAlive());

        return serverBootstrap.bind(BrokerApplication.weEventConfig.getWebSocketPort()).sync().channel();
    }
}
