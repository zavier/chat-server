package com.github.zavier.chat.event;

import io.netty.channel.Channel;

public class UserLogoutEvent {

    private String username;

    private Channel channel;

    public UserLogoutEvent(Channel channel) {
        this.channel = channel;
    }

    public UserLogoutEvent(String username, Channel channel) {
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
