package com.github.zavier.chat.service;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.event.UserLoginEvent;
import com.github.zavier.chat.event.UserLogoutEvent;
import com.github.zavier.chat.room.ChatRoomRepository;
import com.github.zavier.chat.user.LoginUserChannelRegistry;
import io.netty.channel.Channel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserLoginService {

    private final LoginUserChannelRegistry loginUserChannelRegistry;

    public UserLoginService(LoginUserChannelRegistry loginUserChannelRegistry, ChatRoomRepository chatRoomRepository) {
        this.loginUserChannelRegistry = loginUserChannelRegistry;
    }

    @EventListener
    public void userLogoutListener(UserLogoutEvent logoutEvent) {
        final Channel channel = logoutEvent.getChannel();
        final boolean offline = loginUserChannelRegistry.offline(channel.attr(ChatServer.USER_NAME_ATTR_KEY).get());
        if (offline) {
            channel.writeAndFlush(">> Bye bye!");
            if (channel.isOpen()) {
                channel.close();
            }
        }
    }

    @EventListener
    public void userLoginListener(UserLoginEvent loginEvent) {
        // 记录用户登录
        String username = loginEvent.getUsername();
        final Channel channel = loginEvent.getChannel();
        loginUserChannelRegistry.online(channel, username);
    }

}
