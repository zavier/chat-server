package com.github.zavier.chat.user;

import io.netty.channel.Channel;

public class LoginInfo {

    private final String username;

    private final Channel channel;

    public LoginInfo(String username, Channel channel) {
        this.username = username;
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public Channel getChannel() {
        return channel;
    }
}
