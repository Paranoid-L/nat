package com.gitee.leo_92.nat.server.handler;

import com.gitee.leo_92.nat.common.handler.NatCommonHandler;
import com.gitee.leo_92.nat.common.protocol.NatMessage;
import com.gitee.leo_92.nat.common.protocol.NatMessageType;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;

public class RemoteProxyHandler extends NatCommonHandler {
    private final NatCommonHandler proxyHandler;

    public RemoteProxyHandler(NatCommonHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        NatMessage message = new NatMessage();
        message.setType(NatMessageType.CONNECTED);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("channelId", ctx.channel().id().asLongText());
        message.setMetaData(metaData);
        proxyHandler.getCtx().writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        NatMessage message = new NatMessage();
        message.setType(NatMessageType.DISCONNECTED);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("channelId", ctx.channel().id().asLongText());
        message.setMetaData(metaData);
        proxyHandler.getCtx().writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] data = (byte[]) msg;
        NatMessage message = new NatMessage();
        message.setType(NatMessageType.DATA);
        message.setData(data);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("channelId", ctx.channel().id().asLongText());
        message.setMetaData(metaData);
        proxyHandler.getCtx().writeAndFlush(message);
    }
}
