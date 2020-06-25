package com.github.zavier.chat.event;

import io.netty.channel.Channel;

public class UserLoginEvent {

    private String username;

    private Channel channel;

    public UserLoginEvent(String username, Channel channel) {
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
