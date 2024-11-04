package com.gitee.leo_92.nat.client.handler;

import com.gitee.leo_92.nat.client.net.TcpConnection;
import com.gitee.leo_92.nat.client.ClientApplicationRunner;
import com.gitee.leo_92.nat.common.exception.NatException;
import com.gitee.leo_92.nat.common.handler.NatCommonHandler;
import com.gitee.leo_92.nat.common.protocol.NatMessage;
import com.gitee.leo_92.nat.common.protocol.NatMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class NatClientHandler extends NatCommonHandler {
    private final int port;
    private final String password;
    private final String proxyAddress;
    private final int proxyPort;

    private final ConcurrentHashMap<String, NatCommonHandler> channelHandlerMap = new ConcurrentHashMap<>();
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public NatClientHandler(int port, String password, String proxyAddress, int proxyPort) {
        this.port = port;
        this.password = password;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // register client information
        NatMessage natMessage = new NatMessage();
        natMessage.setType(NatMessageType.REGISTER);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("port", port);
        metaData.put("password", password);
        natMessage.setMetaData(metaData);
        ctx.writeAndFlush(natMessage);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NatMessage natMessage = (NatMessage) msg;
        if (natMessage.getType() == NatMessageType.REGISTER_RESULT) {
            processRegisterResult(natMessage);
        } else if (natMessage.getType() == NatMessageType.CONNECTED) {
            processConnected(natMessage);
        } else if (natMessage.getType() == NatMessageType.DISCONNECTED) {
            processDisconnected(natMessage);
        } else if (natMessage.getType() == NatMessageType.DATA) {
            processData(natMessage);
        } else if (natMessage.getType() == NatMessageType.KEEPALIVE) {
            // 心跳包, 不处理
        } else {
            throw new NatException("Unknown type: " + natMessage.getType());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channelGroup.close();
        // 断开连接将状态置换为允许连接
        ClientApplicationRunner.allowConnect = true;
        System.out.println("Loss connection to Nat server, Please restart!");
    }

    /**
     * if natMessage.getType() == NatMessageType.REGISTER_RESULT
     */
    private void processRegisterResult(NatMessage natMessage) {
        if ((Boolean) natMessage.getMetaData().get("success")) {
            System.out.println("Register to Nat server");
        } else {
            System.out.println("Register fail: " + natMessage.getMetaData().get("reason"));
            ctx.close();
        }
    }

    /**
     * if natMessage.getType() == NatMessageType.CONNECTED
     */
    private void processConnected(NatMessage natMessage) throws Exception {

        try {
            NatClientHandler thisHandler = this;
            TcpConnection localConnection = new TcpConnection();
            localConnection.connect(proxyAddress, proxyPort, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    LocalProxyHandler localProxyHandler = new LocalProxyHandler(thisHandler, natMessage.getMetaData().get("channelId").toString());
                    ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder(), localProxyHandler);

                    channelHandlerMap.put(natMessage.getMetaData().get("channelId").toString(), localProxyHandler);
                    channelGroup.add(ch);
                }
            });
        } catch (Exception e) {
            NatMessage message = new NatMessage();
            message.setType(NatMessageType.DISCONNECTED);
            HashMap<String, Object> metaData = new HashMap<>();
            String channelId = natMessage.getMetaData().get("channelId").toString();
            metaData.put("channelId", channelId);
            message.setMetaData(metaData);
            ctx.writeAndFlush(message);
            channelHandlerMap.remove(channelId);
            throw e;
        }
    }

    /**
     * if natMessage.getType() == NatMessageType.DISCONNECTED
     */
    private void processDisconnected(NatMessage natMessage) {
        String channelId = natMessage.getMetaData().get("channelId").toString();
        NatCommonHandler handler = channelHandlerMap.get(channelId);
        if (handler != null) {
            handler.getCtx().close();
            channelHandlerMap.remove(channelId);
        }
    }

    /**
     * if natMessage.getType() == NatMessageType.DATA
     */
    private void processData(NatMessage natMessage) {
        String channelId = natMessage.getMetaData().get("channelId").toString();
        NatCommonHandler handler = channelHandlerMap.get(channelId);
        if (handler != null) {
            ChannelHandlerContext ctx = handler.getCtx();
            ctx.writeAndFlush(natMessage.getData());
        }
    }
}
