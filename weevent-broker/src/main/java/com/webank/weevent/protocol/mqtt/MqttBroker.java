package com.webank.weevent.protocol.mqtt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.protocol.mqtt.command.ProtocolProcess;
import com.webank.weevent.protocol.mqtt.common.MqttWebSocketCodec;
import com.webank.weevent.protocol.mqtt.handler.BrokerHandler;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 **/
@Component
@Slf4j
public class MqttBroker {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel websocketChannel;
    private Channel mqttChannel;

    private ProtocolProcess protocolProcess;

    @Autowired
    public void setProtocolProcess(ProtocolProcess protocolProcess) {
        this.protocolProcess = protocolProcess;
    }

    @PostConstruct
    public void start() throws Exception {
        if (!StringUtils.isBlank(BrokerApplication.weEventConfig.getMqttServerPath())
                && BrokerApplication.weEventConfig.getMqttPort() != null) {
            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();

            // websocket
            this.websocketChannel = webSocketServer(BrokerApplication.weEventConfig.getMqttPort(),
                    BrokerApplication.weEventConfig.getMqttServerPath());
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

    private Channel webSocketServer(int port, String path) throws Exception {
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
                                path,
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

        return serverBootstrap.bind(port).sync().channel();
    }
}
