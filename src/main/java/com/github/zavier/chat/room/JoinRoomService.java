package com.github.zavier.chat.room;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.event.LogoutEvent;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JoinRoomService {

    private static final int ROOM_MAX_USERS = 50;

    private final ConcurrentMap<String, Set<JoinRoomUser>> map = new ConcurrentHashMap<>();

    public void tryJoinRoom(JoinRoomInfo joinRoomInfo) {
        final JoinRoomUser joinRoomUser = new JoinRoomUser(joinRoomInfo.getUsername(), joinRoomInfo.getUserChannel());
        final String roomName = joinRoomInfo.getRoomName();
        synchronized (map) {
            final Set<JoinRoomUser> alreadyJoinUsers = map.get(roomName);
            if (alreadyJoinUsers == null) {
                map.putIfAbsent(roomName, new HashSet<>());
            }
            final Set<JoinRoomUser> joinRoomUsers = map.get(roomName);
            if (joinRoomUsers.size() >= ROOM_MAX_USERS) {
                throw new RuntimeException("char room's user is full, please select another");
            }

            // 加入
            joinRoomUsers.add(joinRoomUser);
        }
    }

    @EventListener
    public void userLogoutListener(LogoutEvent logoutEvent) {
        final String roomName = logoutEvent.getChannel().attr(ChatServer.CHAT_ROOM_ATTR_KEY).get();
        if (roomName != null) {
            final Set<JoinRoomUser> joinRoomUsers = map.get(roomName);
            joinRoomUsers.removeIf(joinRoomUser -> joinRoomUser.getUserName().equalsIgnoreCase(logoutEvent.getUsername()));
        }
    }

    public void quitRoom(QuitRoomInfo quitRoomInfo) {
        final String roomName = quitRoomInfo.getRoomName();
        final Set<JoinRoomUser> joinRoomUsers = map.get(roomName);
        joinRoomUsers.removeIf(joinRoomUser -> joinRoomUser.getUserName().equalsIgnoreCase(quitRoomInfo.getUsername()));
    }

    public Map<String, Channel> getAllUserChannelByRoomName(String roomName) {
        final Set<JoinRoomUser> joinRoomUsers = map.get(roomName);
        if (joinRoomUsers == null) {
            return new HashMap<>();
        }
        return joinRoomUsers.stream()
                .collect(Collectors.toMap(JoinRoomUser::getUserName, JoinRoomUser::getUserChannel));
    }

    public static class JoinRoomUser {
        @Getter
        private final String userName;

        @Getter
        private final Channel userChannel;

        public JoinRoomUser(String userName, Channel userChannel) {
            this.userName = userName;
            this.userChannel = userChannel;
        }

        @Override
        public int hashCode() {
            return userName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof JoinRoomUser) {
                JoinRoomUser joinRoomUser = (JoinRoomUser) obj;
                return Objects.equals(this.getUserName(), joinRoomUser.getUserName());
            }
            return false;
        }
    }
}
