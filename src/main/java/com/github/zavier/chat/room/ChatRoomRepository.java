package com.github.zavier.chat.room;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ChatRoomRepository {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final JoinRoomService joinRoomService;

    public ChatRoomRepository(ApplicationEventPublisher applicationEventPublisher,
                              JoinRoomService joinRoomService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.joinRoomService = joinRoomService;
    }

    private final ConcurrentMap<String, ChatRoom> chatRoomMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        addRoom(new ChatRoom("room1"));
        addRoom(new ChatRoom("room2"));
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

    public void delRoom(ChatRoom chatRoom) {
        String chatRoomName = chatRoom.getChatRoomName();
        if (StringUtils.isBlank(chatRoomName)) {
            log.info("chatRoomName is empty");
            return;
        }
        // 只有聊天室没人在线时才可以删除
        final Map<String, Channel> userChannelByRoomName = joinRoomService.getAllUserChannelByRoomName(chatRoomName);
        if (userChannelByRoomName.isEmpty()) {
            chatRoomMap.remove(chatRoomName);
        } else {
            throw new RuntimeException("room has users now, can't delete");
        }
    }

    public List<ChatRoom> listChatRooms() {
        return new ArrayList<>(chatRoomMap.values());
    }
}
