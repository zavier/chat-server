package com.github.zavier.chat.user;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class LoginUserChannelRegistry {

    private LoginUserChannelRegistry(){}

    private static LoginUserChannelRegistry INSTANCE;

    public static LoginUserChannelRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (LoginUserChannelRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoginUserChannelRegistry();
                }
            }
        }
        return INSTANCE;
    }

    private final ConcurrentMap<String, Channel> userNameChannelMap = new ConcurrentHashMap<>();

    public void online(Channel channel, String userName) {
        userNameChannelMap.put(userName, channel);
        log.info("{} online", userName);
    }

    public void offline(String username) {
        if (StringUtils.isBlank(username)) {
            return;
        }
        userNameChannelMap.remove(username);
        log.info("{} offline", username);
    }

    public boolean isLogin(String username) {
        return userNameChannelMap.containsKey(username);
    }

    public Map<String, Channel> getOnlineUserInfoMap() {
        return new HashMap<>(userNameChannelMap);
    }
}
