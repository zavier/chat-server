package com.github.zavier.chat.handler;

import com.github.zavier.chat.ChatServer;
import com.github.zavier.chat.user.LoginUserChannelRegistry;
import com.github.zavier.chat.user.NetUser;
import com.github.zavier.chat.user.NetUserRepository;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户登录/注册/退出 相关逻辑处理Hanlder
 *
 */
@Slf4j
public class LoginHandler extends SimpleChannelInboundHandler<String> {

    private static final Set<String> EXIT_CMD = initExitCmd();

    private static Set<String> initExitCmd() {
        Set<String> exitCmdSet = new HashSet<>();
        exitCmdSet.add("exit");
        exitCmdSet.add("quit");
        return exitCmdSet;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendInputUsernameMessage(ctx.channel());
    }

    private void sendInputUsernameMessage(Channel channel) {
        sendMessage(channel, ">> please input your username:\n");
    }

    private void sendInputPasswordMessage(Channel channel) {
        sendMessage(channel, ">> please input your password:\n");
    }

    private void sendLoginSuccessMessage(Channel channel) {
        sendMessage(channel, ">> login success!\n");
    }

    private void sendPasswordIncorrectMessage(Channel channel) {
        sendMessage(channel, ">> username or password incorrect!\n");
    }

    private void sendRegisterSuccessMessage(Channel channel) {
        sendMessage(channel, ">> register success!\n");
    }

    private void sendMessage(Channel channel, String message) {
        channel.writeAndFlush(message);
    }

    private boolean sendExitMessageAndCloseIfNeed(ChannelHandlerContext ctx, String receiveMsg) {
        for (String exitCmd : EXIT_CMD) {
            if (exitCmd.equalsIgnoreCase(receiveMsg)) {
                ctx.writeAndFlush(">> Bye bye!\n");
                ctx.close();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        // 空消息不处理
        if (StringUtils.isBlank(msg)) {
            // 此时如果没有输入用户名则提示
            if (channel.attr(ChatServer.USER_NAME_ATTR_KEY).get() == null) {
                sendInputUsernameMessage(channel);
            }
            return;
        }

        // 关闭消息则进行连接关闭
        if (sendExitMessageAndCloseIfNeed(ctx, msg)) {
            return;
        }

        // 没有用户名则设置channel中的用户名, 并请求输入密码
        if (channel.attr(ChatServer.USER_NAME_ATTR_KEY).get() == null) {
            channel.attr(ChatServer.USER_NAME_ATTR_KEY).setIfAbsent(msg);
            sendInputPasswordMessage(channel);
            return;
        }

        LoginUserChannelRegistry instance = LoginUserChannelRegistry.getInstance();
        String username = channel.attr(ChatServer.USER_NAME_ATTR_KEY).get();
        boolean login = instance.isLogin(username);
        // 已登录成功则直接进行后续逻辑处理
        if (login) {
            ctx.fireChannelRead(msg);
            return;
        }

        // 校验用户名密码，如果用户名不存在则进行注册
        NetUserRepository netUserRepository = NetUserRepository.getInstance();
        // 用户名存在则进行登录操作
        NetUser netUser = new NetUser(username, msg);
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
            NetUserRepository netUserRepository = NetUserRepository.getInstance();
            netUserRepository.registry(netUser);
            sendRegisterSuccessMessage(channel);
        } catch (Exception e) {
            // 可能注册冲突
            sendMessage(channel, e.getMessage());
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
        NetUserRepository netUserRepository = NetUserRepository.getInstance();
        boolean checkPassword = netUserRepository.checkPassword(netUser);
        // 校验通过
        if (checkPassword) {
            LoginUserChannelRegistry userChannelRegistry = LoginUserChannelRegistry.getInstance();
            String username = netUser.getUsername();
            userChannelRegistry.online(channel, username);
            sendLoginSuccessMessage(channel);
            return true;
        }

        // 密码校验失败，发送消息，清空保存的用户名
        sendPasswordIncorrectMessage(channel);
        channel.attr(ChatServer.USER_NAME_ATTR_KEY).set(null);
        sendInputUsernameMessage(channel);
        return false;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LoginUserChannelRegistry instance = LoginUserChannelRegistry.getInstance();
        instance.offline(ctx.channel().attr(ChatServer.USER_NAME_ATTR_KEY).get());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String username = ctx.channel().attr(ChatServer.USER_NAME_ATTR_KEY).get();
        log.warn("{} error occurred:{} ", username, cause.getMessage());
        ctx.channel().close();
    }
}
