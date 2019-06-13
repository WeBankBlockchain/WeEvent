package com.webank.weevent.protocol.mqttbroker;

import java.io.InputStream;
import java.security.KeyStore;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.protocol.mqttbroker.common.MqttWebSocketCodec;
import com.webank.weevent.protocol.mqttbroker.handler.BrokerHandler;
import com.webank.weevent.protocol.mqttbroker.mqttprotocol.ProtocolProcess;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
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
public class BrokerServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SslContext sslContext;
    private Channel websocketChannel;
    @Autowired
    private ProtocolProcess protocolProcess;

    @PostConstruct
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("server.p12");
        keyStore.load(inputStream, "123456".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "123456".toCharArray());
        sslContext = SslContextBuilder.forServer(kmf).build();
        mqttServer();
        webSocketServer();
    }

    @PreDestroy
    public void stop() {
        bossGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup.shutdownGracefully();
        workerGroup = null;
        websocketChannel.closeFuture().syncUninterruptibly();
        websocketChannel = null;
    }

    private SslHandler getSslHandler(SslContext sslContext, ByteBufAllocator byteBufAllocator) {
        SSLEngine sslEngine = sslContext.newEngine(byteBufAllocator);
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return new SslHandler(sslEngine);
    }

    private void mqttServer() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        channelPipeline.addFirst("idle", new IdleStateHandler(0, 0, BrokerApplication.weEventConfig.getKeepAlive()));
                        if (BrokerApplication.weEventConfig.getSslEnable().equals("true")) {
                            channelPipeline.addLast("ssl", getSslHandler(sslContext, socketChannel.alloc()));
                        }
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, BrokerApplication.weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, BrokerApplication.weEventConfig.getSoKeepAlive());
        serverBootstrap.bind(BrokerApplication.weEventConfig.getBrokerServerPort()).sync().channel();
    }

    private void webSocketServer() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        channelPipeline.addFirst("idle", new IdleStateHandler(0, 0, BrokerApplication.weEventConfig.getKeepAlive()));
                        if (BrokerApplication.weEventConfig.getSslEnable().equals("true")) {
                            channelPipeline.addLast("ssl", getSslHandler(sslContext, socketChannel.alloc()));
                        }
                        channelPipeline.addLast("http-codec", new HttpServerCodec());
                        channelPipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                        channelPipeline.addLast("compressor ", new HttpContentCompressor());
                        channelPipeline.addLast("protocol", new WebSocketServerProtocolHandler(BrokerApplication.weEventConfig.getWebSocketServerPath(), "mqtt,mqttv3.1,mqttv3.1.1", true, 65536));
                        channelPipeline.addLast("mqttWebSocket", new MqttWebSocketCodec());
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, BrokerApplication.weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, BrokerApplication.weEventConfig.getSoKeepAlive());
        websocketChannel = serverBootstrap.bind(BrokerApplication.weEventConfig.getWebSocketPort()).sync().channel();
    }
}
