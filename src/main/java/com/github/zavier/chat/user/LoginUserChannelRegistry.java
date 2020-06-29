package com.github.zavier.chat.user;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class LoginUserChannelRegistry {

    private final ConcurrentMap<String, Channel> userNameChannelMap = new ConcurrentHashMap<>();

    public boolean online(Channel channel, String userName) {
        final Channel put = userNameChannelMap.put(userName, channel);
        if (put == null) {
            log.info("{} online", userName);
            return true;
        }
        return false;
    }

    public boolean offline(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        final Channel remove = userNameChannelMap.remove(username);
        if (remove != null) {
            log.info("{} offline", username);
            return true;
        }
        return false;
    }

    public boolean isLogin(String username) {
        return userNameChannelMap.containsKey(username);
    }

    public Channel getUserChannel(String username) {
        return userNameChannelMap.get(username);
    }

    public Map<String, Channel> getOnlineUserInfoMap() {
        return new HashMap<>(userNameChannelMap);
    }
}
