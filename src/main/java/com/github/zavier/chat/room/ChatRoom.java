package com.github.zavier.chat.room;

import lombok.Data;

import java.util.List;

@Data
public class ChatRoom {

    private String chatRoomName;

    private List<String> userNameList;

    public ChatRoom(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }
}
