package com.github.zavier.chat;

import com.github.zavier.chat.codec.StringLineBasedFrameDecoder;
import com.github.zavier.chat.codec.StringMessageEncoder;
import com.github.zavier.chat.handler.MessageHandler;
import com.github.zavier.chat.handler.TimeoutHandler;
import com.github.zavier.chat.user.UserStatus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ChatServer {

    public static final AttributeKey<String> USER_NAME_ATTR_KEY = AttributeKey.newInstance("USER_NAME_KEY");

    public static final AttributeKey<String> CHAT_ROOM_ATTR_KEY = AttributeKey.newInstance("CHAT_ROOM_KEY");

    public static final AttributeKey<UserStatus> USER_STATUS_ATTR_KEY = AttributeKey.newInstance("USER_STATUS_KEY");

    public void startServer(int port) {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("readTimeout", new ReadTimeoutHandler(6, TimeUnit.HOURS));
                            pipeline.addLast("timeoutHandler", new TimeoutHandler());
//                            pipeline.addLast("log", new LoggingHandler(LogLevel.INFO));
                            pipeline.addLast("decoder", new StringLineBasedFrameDecoder(5 * 1024));
                            pipeline.addLast("encoder", new StringMessageEncoder());
                            pipeline.addLast("handler", new MessageHandler());
                        }
                    });
            // 同步绑定
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            log.info("Chat Server 启动成功 start success 端口:{}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            // netty 启动失败，立即报错，禁止项目继续运行
            log.error("Chat Server 启动失败", e);
            throw new RuntimeException("应用启动失败");
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
