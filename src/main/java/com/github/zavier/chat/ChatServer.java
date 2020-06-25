package com.github.zavier.chat;

import com.github.zavier.chat.codec.StringLineBasedFrameDecoder;
import com.github.zavier.chat.codec.StringMessageEncoder;
import com.github.zavier.chat.room.ChatRoom;
import com.github.zavier.chat.room.ChatRoomRepository;
import com.github.zavier.chat.handler.*;
import com.github.zavier.chat.user.NetUser;
import com.github.zavier.chat.user.NetUserRepository;
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

    public void initData() {
        // 初始化聊天室
        ChatRoom chatRoom = new ChatRoom("chat root 1");
        ChatRoomRepository instance = ChatRoomRepository.getInstance();
        instance.addRoom(chatRoom);

        // 初始化用户
        NetUserRepository userRepository = NetUserRepository.getInstance();
        NetUser netUser = new NetUser("admin", "admin");
        NetUser netUser1 = new NetUser("lisi", "test");
        NetUser netUser2 = new NetUser("zhangsan", "test");
        NetUser netUser3 = new NetUser("wangwu", "test");
        userRepository.registry(netUser);
        userRepository.registry(netUser1);
        userRepository.registry(netUser2);
        userRepository.registry(netUser3);
    }

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
                            pipeline.addLast("loginHandler", new LoginHandler());
                            pipeline.addLast("server", new DispatcherServerHandler());
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
