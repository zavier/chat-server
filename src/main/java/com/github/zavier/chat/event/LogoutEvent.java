package com.github.zavier.chat.event;

import io.netty.channel.Channel;

public class LogoutEvent {

    private final String username;

    private final Channel channel;

    public LogoutEvent(String username, Channel channel) {
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
