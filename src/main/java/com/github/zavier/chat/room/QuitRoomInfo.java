package com.github.zavier.chat.room;

import io.netty.channel.Channel;

public class QuitRoomInfo {

    private String username;

    private String roomName;

    private Channel userChannel;

    public QuitRoomInfo(String username, String roomName, Channel userChannel) {
        this.username = username;
        this.roomName = roomName;
        this.userChannel = userChannel;
    }

    public String getUsername() {
        return username;
    }

    public String getRoomName() {
        return roomName;
    }

    public Channel getUserChannel() {
        return userChannel;
    }
}
