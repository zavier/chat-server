package com.github.zavier.chat.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于控制客户端超时关闭
 */
@Slf4j
@ChannelHandler.Sharable
public class TimeoutHandler extends ChannelDuplexHandler {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            ctx.writeAndFlush("time out, bye~");
            ctx.close();
        }
    }
}
