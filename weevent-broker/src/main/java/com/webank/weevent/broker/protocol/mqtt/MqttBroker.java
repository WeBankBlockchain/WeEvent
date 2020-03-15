package com.webank.weevent.broker.protocol.mqtt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.webank.weevent.broker.config.WeEventConfig;

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
@Slf4j
@Component
public class MqttBroker {
    private WeEventConfig weEventConfig;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel websocketChannel;
    private Channel tcpChannel;

    private ProtocolProcess protocolProcess;

    @Autowired
    public void setWeEventConfig(WeEventConfig weEventConfig) {
        this.weEventConfig = weEventConfig;
    }

    @Autowired
    public void setProtocolProcess(ProtocolProcess protocolProcess) {
        this.protocolProcess = protocolProcess;
    }

    @PostConstruct
    public void start() throws Exception {
        // websocket
        if (!StringUtils.isBlank(this.weEventConfig.getMqttServerPath())
                && this.weEventConfig.getMqttPort() > 0) {
            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();

            this.websocketChannel = webSocketServer(this.weEventConfig.getMqttPort(),
                    this.weEventConfig.getMqttServerPath());
        }

        // tcp
        if (this.weEventConfig.getMqttTcpPort() > 0) {
            if (this.bossGroup == null) {
                this.bossGroup = new NioEventLoopGroup();
            }
            if (this.workerGroup == null) {
                this.workerGroup = new NioEventLoopGroup();
            }

            this.tcpChannel = tcpServer(this.weEventConfig.getMqttTcpPort());
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

        if (this.websocketChannel != null) {
            this.websocketChannel.closeFuture().syncUninterruptibly();
            this.websocketChannel = null;
        }

        if (this.tcpChannel != null) {
            this.tcpChannel.closeFuture().syncUninterruptibly();
            this.tcpChannel = null;
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
                                weEventConfig.getKeepAlive()));
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
                .option(ChannelOption.SO_BACKLOG, this.weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, this.weEventConfig.getSoKeepAlive());

        return serverBootstrap.bind(port).sync().channel();
    }

    private Channel tcpServer(int port) throws Exception {
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
                                weEventConfig.getKeepAlive()));

                        //channelPipeline.addLast("ssl", getSslHandler(sslContext, socketChannel.alloc()));
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, weEventConfig.getSoKeepAlive());
        return serverBootstrap.bind(port).sync().channel();
    }
}
