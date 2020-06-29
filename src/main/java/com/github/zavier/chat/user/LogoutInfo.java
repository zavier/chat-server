package com.github.zavier.chat.user;

import io.netty.channel.Channel;

public class LogoutInfo {

    private final Channel channel;

    public LogoutInfo(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
