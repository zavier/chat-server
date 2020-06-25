package com.github.zavier.chat.handler;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.user.LogoutInfo;
import com.github.zavier.chat.user.UserStatus;
import com.github.zavier.chat.util.SpringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 消息处理器
 *
 */
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 初始状态设置
        ctx.channel().attr(ChatServer.USER_STATUS_ATTR_KEY).set(UserStatus.TYPING_USER_NAME);
        UserStatus.TYPING_USER_NAME.sendNotifyUserInfo(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final ApplicationEventPublisher eventPublisher = SpringUtil.getEventPublisher();
        eventPublisher.publishEvent(new LogoutInfo(ctx.channel()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.debug("read len:{}, content:{}", msg.length(), msg);

        UserStatus userStatus = ctx.channel().attr(ChatServer.USER_STATUS_ATTR_KEY).get();
        userStatus.handlerMessage(ctx.channel(), msg);

        // 状态改变了,进行提示信息发送
        while (userStatus != ctx.channel().attr(ChatServer.USER_STATUS_ATTR_KEY).get()) {
            userStatus = ctx.channel().attr(ChatServer.USER_STATUS_ATTR_KEY).get();
            userStatus.sendNotifyUserInfo(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String username = ctx.channel().attr(ChatServer.USER_NAME_ATTR_KEY).get();
        log.error("{} error occurred ", username, cause);
        ctx.channel().close();
    }

}
