package com.github.zavier.chat.user;


import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.room.*;
import com.github.zavier.chat.util.SpringUtil;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum UserStatus {


    /**
     * 输入用户名中
     */
    TYPING_USER_NAME() {
        @Override
        public void sendNotifyUserInfo(Channel channel) {
            String message = ">> please input your username:";
            channel.writeAndFlush(message);
        }

        @Override
        public void doHandlerMessage(Channel channel, String message) {
            channel.attr(ChatServer.USER_NAME_ATTR_KEY).setIfAbsent(message);
            // 设置为请求输入密码态
            channel.attr(ChatServer.USER_STATUS_ATTR_KEY).set(UserStatus.TYPING_PASSWORD);
        }
    },

    /**
     * 输入密码中
     */
    TYPING_PASSWORD() {
        @Override
        public void sendNotifyUserInfo(Channel channel) {
            String message = ">> please input your password:";
            channel.writeAndFlush(message);
        }


        @Override
        public void doHandlerMessage(Channel channel, String message) {
            // 校验用户名密码，如果用户名不存在则进行注册
            NetUserRepository netUserRepository = SpringUtil.getBean(NetUserRepository.class);
            String username = channel.attr(ChatServer.USER_NAME_ATTR_KEY).get();
            NetUser netUser = new NetUser(username, message);
            if (netUserRepository.existUsername(username)) {
                tryLogin(channel, netUser);
            } else {
                // 进行注册操作（用户名不存在）
                final boolean registerSuc = tryRegister(channel, netUser);
                if (registerSuc) {
                    tryLogin(channel, netUser);
                }
            }
        }

        private boolean tryRegister(Channel channel, NetUser netUser) {
            try {
                NetUserRepository netUserRepository = SpringUtil.getBean(NetUserRepository.class);
                netUserRepository.registry(netUser);
                channel.writeAndFlush(">> register success!");
            } catch (Exception e) {
                // 可能注册冲突
                channel.writeAndFlush(e.getMessage());
                return false;
            }
            return true;
        }

        /**
         * 尝试登录
         *
         * @param channel 连接
         * @param netUser 用户信息
         */
        private boolean tryLogin(Channel channel, NetUser netUser) {
            NetUserRepository netUserRepository = SpringUtil.getBean(NetUserRepository.class);
            boolean checkPassword = netUserRepository.checkPassword(netUser);
            // 校验通过
            if (checkPassword) {
                // 发送登录成功事件
                channel.writeAndFlush(">> login success!");
                // 修改状态为选择聊天室
                channel.attr(ChatServer.USER_STATUS_ATTR_KEY).set(UserStatus.SELECTING_CHAT_ROOM);
                // 登录
                SpringUtil.getBean(UserLoginService.class).userLogin(new LoginInfo(netUser.getUsername(), channel));
                return true;
            }

            // 密码校验失败，发送消息，清空保存的用户名
            channel.writeAndFlush(">> username or password incorrect!");
            channel.attr(ChatServer.USER_NAME_ATTR_KEY).set(null);
            // 重置为重新输入用户名态
            channel.attr(ChatServer.USER_STATUS_ATTR_KEY).set(UserStatus.TYPING_USER_NAME);
            return false;
        }
    },

    /**
     * 选择聊天室状态
     */
    SELECTING_CHAT_ROOM() {
        @Override
        public void sendNotifyUserInfo(Channel channel) {
            channel.writeAndFlush(listAddChatRoomMessage());
        }

        @Override
        protected void doHandlerMessage(Channel channel, String message) {
            final ChatRoomRepository chatRoomRepository = SpringUtil.getBean(ChatRoomRepository.class);
            final List<ChatRoom> chatRooms = chatRoomRepository.listChatRooms();
            final Optional<ChatRoom> selectRoomOptional = chatRooms.stream()
                    .filter(chatRoom -> Objects.equals(chatRoom.getChatRoomName(), message))
                    .findFirst();
            if (selectRoomOptional.isPresent()) {
                // 尝试加入聊天室事件
                final String username = channel.attr(ChatServer.USER_NAME_ATTR_KEY).get();
                try {
                    final JoinRoomService joinRoomService = SpringUtil.getBean(JoinRoomService.class);
                    joinRoomService.tryJoinRoom(new JoinRoomInfo(username, message, channel));
                } catch (Exception e) {
                    channel.writeAndFlush(e.getMessage());
                    return;
                }
                // 加入聊天室成功，发送消息，修改状态
                channel.writeAndFlush(">> Enter " + message + " success");
                channel.attr(ChatServer.USER_STATUS_ATTR_KEY).set(UserStatus.CHATING);
                // 绑定用户所在聊天室
                channel.attr(ChatServer.CHAT_ROOM_ATTR_KEY).set(message);
            } else {
                channel.writeAndFlush(">> please input a correct room name ");
            }
        }

        private String listAddChatRoomMessage() {
            final ChatRoomRepository chatRoomRepository = SpringUtil.getBean(ChatRoomRepository.class);
            final List<ChatRoom> chatRooms = chatRoomRepository.listChatRooms();
            if (chatRooms.isEmpty()) {
                return ">> sorry, There is no chat room to join...";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(">> please select a chat room (input name):\n");
            for (int i = 0; i < chatRooms.size(); i++) {
                final ChatRoom chatRoom = chatRooms.get(i);
                sb.append("-- ")
                        .append(i + 1).append(". ")
                        .append(chatRoom.getChatRoomName())
                        .append("\n");
            }
            return sb.toString();
        }
    },

    /**
     * 正常聊天中
     */
    CHATING() {
        @Override
        public void sendNotifyUserInfo(Channel channel) {
            // 不需要发送消息
        }

        @Override
        protected void doHandlerMessage(Channel channel, String message) {
            final String username = channel.attr(ChatServer.USER_NAME_ATTR_KEY).get();
            String sendMessage = ">> " + username + ": " + message + "\r";

            final String roomName = channel.attr(ChatServer.CHAT_ROOM_ATTR_KEY).get();
            final Map<String, Channel> allUserChannelByRoomName = SpringUtil.getBean(JoinRoomService.class).getAllUserChannelByRoomName(roomName);
            if (allUserChannelByRoomName != null) {
                allUserChannelByRoomName.forEach((name, ch) -> {
                    ch.writeAndFlush(sendMessage);
                });
            }
        }
    },

    ;

    private static final Set<String> EXIT_CMD = initExitCmd();

    private static Set<String> initExitCmd() {
        Set<String> exitCmdSet = new HashSet<>();
        exitCmdSet.add("exit");
        exitCmdSet.add("quit");
        return exitCmdSet;
    }

    private boolean checkAndHandlerExitAndInvalidMessage(Channel channel, String message) {
        // 空消息不处理
        if (StringUtils.isBlank(message)) {
            // 根据状态进行提示
            final UserStatus userStatus = channel.attr(ChatServer.USER_STATUS_ATTR_KEY).get();
            if (userStatus != null) {
                userStatus.sendNotifyUserInfo(channel);
            }
            return true;
        }


        for (String exitCmd : EXIT_CMD) {
            if (exitCmd.equalsIgnoreCase(message)) {
                // 如果在聊天室中则退出聊天室，否则整个退出
                if (channel.attr(ChatServer.CHAT_ROOM_ATTR_KEY).get() != null) {
                    final String username = channel.attr(ChatServer.USER_NAME_ATTR_KEY).get();
                    final String roomName = channel.attr(ChatServer.CHAT_ROOM_ATTR_KEY).get();
                    channel.attr(ChatServer.CHAT_ROOM_ATTR_KEY).set(null);
                    SpringUtil.getBean(JoinRoomService.class).quitRoom(new QuitRoomInfo(username, roomName, channel));
                    channel.attr(ChatServer.USER_STATUS_ATTR_KEY).set(UserStatus.SELECTING_CHAT_ROOM);
                } else {
                    SpringUtil.getBean(UserLoginService.class).userLogout(new LogoutInfo(channel));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 发送给用户的提示信息
     *
     * @param channel 消息通道
     */
    public abstract void sendNotifyUserInfo(Channel channel);

    /**
     * 处理消息
     *
     * @param channel 消息通道
     * @param message 消息内容
     */
    public final void handlerMessage(Channel channel, String message) {
        final boolean hit = checkAndHandlerExitAndInvalidMessage(channel, message);
        if (hit) {
            return;
        }
        doHandlerMessage(channel, message);
    }

    /**
     * 子类进行消息处理
     *
     * @param channel 通道
     * @param message 消息内容
     */
    protected abstract void doHandlerMessage(Channel channel, String message);
}
