package com.github.zavier.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class StringLineBasedFrameDecoder extends LineBasedFrameDecoder {

    public StringLineBasedFrameDecoder(int maxLength) {
        super(maxLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
        if (frame == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        while (frame.readableBytes() > 0) {
            char c = (char) frame.readByte();
            sb.append(c);
        }
        return sb.toString();
    }
}
