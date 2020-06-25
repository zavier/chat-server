package com.github.zavier.chat.room;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ChatRoomRepository {

    private final ConcurrentMap<String, ChatRoom> chatRoomMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        addRoom(new ChatRoom("testRoo1"));
    }

    public void addRoom(ChatRoom chatRoom) {
        String chatRoomName = chatRoom.getChatRoomName();
        if (StringUtils.isBlank(chatRoomName)) {
            log.info("chatRoomName is empty");
            return;
        }
        if (chatRoomMap.containsKey(chatRoomName)) {
            log.info("chatRoomName:{} is already existed", chatRoomName);
            return;
        }
        chatRoomMap.put(chatRoomName, chatRoom);
    }

    public List<ChatRoom> listChatRooms() {
        return new ArrayList<>(chatRoomMap.values());
    }
}
