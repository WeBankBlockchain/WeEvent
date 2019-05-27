package com.webank.weevent.protocol.mqttbroker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.protocol.mqttbroker.protocol.ProtocolProcess;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author websterchen
 * @version 1.0
 * @since 20/05/2019
 */
@Slf4j
@Component
public class BrokerServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /*@Autowired
    private ProtocolProcess protocolProcess;*/

    @PostConstruct
    public void start() throws Exception{
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        //mqttServer();
        //websocketServer();
    }

    @PreDestroy
    public void stop() {
        this.bossGroup.shutdownGracefully();
        this.bossGroup = null;
        this.workerGroup.shutdownGracefully();
        this.bossGroup = null;
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
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        //channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, BrokerApplication.weEventConfig.getSoBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, BrokerApplication.weEventConfig.getSoKeepAlive());
        serverBootstrap.bind(BrokerApplication.weEventConfig.getBrokerServerPort()).sync().channel();
    }

    private void websocketServer() {

    }
}
