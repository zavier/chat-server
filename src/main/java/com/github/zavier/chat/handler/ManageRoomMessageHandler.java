package com.github.zavier.chat.handler;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.room.ChatRoom;
import com.github.zavier.chat.room.ChatRoomRepository;
import com.github.zavier.chat.util.SpringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Objects;

/**
 * 管理房间的命令处理器
 *
 */
public class ManageRoomMessageHandler extends ChannelInboundHandlerAdapter {

    private static final String ADMIN_USER_NAME = "admin";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final String username = ctx.channel().attr(ChatServer.USER_NAME_ATTR_KEY).get();
        if (!Objects.equals(username, ADMIN_USER_NAME)) {
            ctx.fireChannelRead(msg);
            return;
        }

        String message = (String) msg;
        final String[] manageRoomCmd = message.split(" ");
        String cmdPrefix = "cmd";
        if (message.startsWith(cmdPrefix) && manageRoomCmd.length == 3) {
            final String cmd = manageRoomCmd[1];
            final String roomName = manageRoomCmd[2];
            final ChatRoomRepository chatRoomRepository = SpringUtil.getBean(ChatRoomRepository.class);
            switch (cmd) {
                case "add":
                    chatRoomRepository.addRoom(new ChatRoom(roomName));
                    ctx.writeAndFlush("add room success");
                    break;
                case "del":
                    try {
                        chatRoomRepository.delRoom(new ChatRoom(roomName));
                        ctx.writeAndFlush("del room success");
                    } catch (Exception e) {
                        ctx.writeAndFlush(e.getMessage());
                    }
                    break;
                default:
                    ctx.writeAndFlush("cmd format error");
            }
            return;
        }

        ctx.fireChannelRead(msg);
    }

}
