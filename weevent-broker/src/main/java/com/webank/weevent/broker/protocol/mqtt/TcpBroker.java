package com.webank.weevent.broker.protocol.mqtt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.webank.weevent.broker.config.WeEventConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
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
 * @version v1.0
 * @since 2019/6/2
 */
@Slf4j
@Component
public class TcpBroker {
    private WeEventConfig weEventConfig;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
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
        if (this.tcpChannel != null) {
            return;
        }

        // tcp
        if (this.weEventConfig.getMqttTcpPort() > 0) {
            log.info("setup MQTT over tcp on port: {}", this.weEventConfig.getMqttTcpPort());

            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();
            this.tcpChannel = tcpServer(this.weEventConfig.getMqttTcpPort());
        }
    }

    @PreDestroy
    public void stop() {
        if (this.tcpChannel == null) {
            return;
        }

        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        this.tcpChannel.closeFuture().syncUninterruptibly();
        this.tcpChannel = null;
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
                        channelPipeline.addLast("broker", new TcpHandler(protocolProcess));
                    }
                });
        return serverBootstrap.bind(port).sync().channel();
    }
}
