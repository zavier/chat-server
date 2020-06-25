package com.github.zavier;

import com.github.zavier.chat.ChatServer;

public class Main {
    public static void main(String[] args) {
        int port = 1234;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ChatServer chatServer = new ChatServer();
        chatServer.startServer(port);
    }
}
