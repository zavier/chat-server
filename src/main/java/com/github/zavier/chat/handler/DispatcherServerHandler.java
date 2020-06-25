package com.github.zavier.chat.handler;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.user.LoginUserChannelRegistry;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DispatcherServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("read len:{}, content:{}", msg.length(), msg);

        final String username = ctx.channel().attr(ChatServer.USER_NAME_ATTR_KEY).get();
        String message = ">> " + username + ": " + msg + "\r\n";

        LoginUserChannelRegistry instance = LoginUserChannelRegistry.getInstance();
        Map<String, Channel> onlineUserInfoMap = instance.getOnlineUserInfoMap();
        onlineUserInfoMap.forEach((name, channel) -> {
            channel.writeAndFlush(message);
        });

    }


}
