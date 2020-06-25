package com.github.zavier.chat.user;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NetUserRepository {

    private final ConcurrentMap<String, NetUser> netUserMap = new ConcurrentHashMap<>();

    private NetUserRepository() {}

    private static NetUserRepository INSTANCE;

    public static NetUserRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (NetUserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetUserRepository();
                }
            }
        }
        return INSTANCE;
    }

    public void registry(NetUser netUser) {
        String userName = netUser.getUsername();
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("用户名称不能为空");
        }
        final NetUser existUser = netUserMap.putIfAbsent(userName, netUser);
        if (existUser != null) {
            throw new IllegalArgumentException("用户名称已存在");
        }

    }

    public boolean checkPassword(NetUser netUser) {
        NetUser existUser = netUserMap.get(netUser.getUsername());
        if (existUser == null) {
            return false;
        }
        return Objects.equals(existUser.getPassword(), netUser.getPassword());
    }

    public boolean existUsername(String username) {
        return netUserMap.containsKey(username);
    }
}
