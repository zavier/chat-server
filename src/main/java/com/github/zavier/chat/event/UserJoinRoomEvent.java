package com.github.zavier.chat.event;

public class UserJoinRoomEvent {

    private String username;

    private String roomName;

    public UserJoinRoomEvent(String username, String roomName) {
        this.username = username;
        this.roomName = roomName;
    }
}
