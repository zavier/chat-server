package com.github.zavier.chat.user;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.event.LogoutEvent;
import com.github.zavier.chat.room.ChatRoomRepository;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class UserLoginService {

    private final LoginUserChannelRegistry loginUserChannelRegistry;

    private final ApplicationEventPublisher applicationEventPublisher;

    public UserLoginService(LoginUserChannelRegistry loginUserChannelRegistry, ChatRoomRepository chatRoomRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.loginUserChannelRegistry = loginUserChannelRegistry;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void userLogout(LogoutInfo logoutInfo) {
        final Channel channel = logoutInfo.getChannel();
        final String username = channel.attr(ChatServer.USER_NAME_ATTR_KEY).get();

        // 用户还没有登录,直接关闭即可
        if (StringUtils.isBlank(username) || !loginUserChannelRegistry.isLogin(username)) {
            channel.writeAndFlush(">> Bye bye!");
            if (channel.isOpen()) {
                channel.close();
            }
            return;
        }

        final boolean offline = loginUserChannelRegistry.offline(username);
        if (offline) {
            channel.writeAndFlush(">> Bye bye!");
            if (channel.isOpen()) {
                channel.close();
            }
            // 发送事件
            applicationEventPublisher.publishEvent(new LogoutEvent(username, logoutInfo.getChannel()));
        }
    }

    public void userLogin(LoginInfo loginEvent) {
        // 记录用户登录
        String username = loginEvent.getUsername();
        final Channel channel = loginEvent.getChannel();
        loginUserChannelRegistry.online(channel, username);
    }

}
