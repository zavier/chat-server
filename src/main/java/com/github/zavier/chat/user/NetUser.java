package com.github.zavier.chat.user;

import lombok.Data;

@Data
public class NetUser {

    private String username;

    private String password;

    public NetUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
