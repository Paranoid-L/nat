package com.gitee.leo_92.nat.server.handler;

import com.gitee.leo_92.nat.common.exception.NatException;
import com.gitee.leo_92.nat.common.handler.NatCommonHandler;
import com.gitee.leo_92.nat.common.protocol.NatMessage;
import com.gitee.leo_92.nat.common.protocol.NatMessageType;
import com.gitee.leo_92.nat.server.net.TcpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;

public class NatServerHandler extends NatCommonHandler {
    private final TcpServer remoteConnectionServer = new TcpServer();

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final String password;

    private int port;

    private boolean register = false;

    public NatServerHandler(String password) {
        this.password = password;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        NatMessage natMessage = (NatMessage) msg;
        if (natMessage.getType() == NatMessageType.REGISTER) {
            processRegister(natMessage);
        } else if (register) {
            if (natMessage.getType() == NatMessageType.DISCONNECTED) {
                processDisconnected(natMessage);
            } else if (natMessage.getType() == NatMessageType.DATA) {
                processData(natMessage);
            } else if (natMessage.getType() == NatMessageType.KEEPALIVE) {
                // 心跳包, 不处理
            } else {
                throw new NatException("Unknown type: " + natMessage.getType());
            }
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        remoteConnectionServer.close();
        if (register) {
            System.out.println("Stop server on port: " + port);
        }
    }

    /**
     * if natMessage.getType() == NatMessageType.REGISTER
     */
    private void processRegister(NatMessage natMessage) {
        HashMap<String, Object> metaData = new HashMap<>();

        String password = natMessage.getMetaData().get("password").toString();
        if (this.password != null && !this.password.equals(password)) {
            metaData.put("success", false);
            metaData.put("reason", "Token is wrong");
        } else {
            int port = (int) natMessage.getMetaData().get("port");

            try {

                NatServerHandler thisHandler = this;
                remoteConnectionServer.bind(port, new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder(), new RemoteProxyHandler(thisHandler));
                        channels.add(ch);
                    }
                });

                metaData.put("success", true);
                this.port = port;
                register = true;
                System.out.println("Register success, start server on port: " + port);
            } catch (Exception e) {
                metaData.put("success", false);
                metaData.put("reason", e.getMessage());
                e.printStackTrace();
            }
        }

        NatMessage sendBackMessage = new NatMessage();
        sendBackMessage.setType(NatMessageType.REGISTER_RESULT);
        sendBackMessage.setMetaData(metaData);
        ctx.writeAndFlush(sendBackMessage);

        if (!register) {
            System.out.println("Client register error: " + metaData.get("reason"));
            ctx.close();
        }
    }

    /**
     * if natMessage.getType() == NatMessageType.DATA
     */
    private void processData(NatMessage natMessage) {
        channels.writeAndFlush(natMessage.getData(), channel -> channel.id().asLongText().equals(natMessage.getMetaData().get("channelId")));
    }

    /**
     * if natMessage.getType() == NatMessageType.DISCONNECTED
     *
     * @param natMessage message
     */
    private void processDisconnected(NatMessage natMessage) {
        channels.close(channel -> channel.id().asLongText().equals(natMessage.getMetaData().get("channelId")));
    }
}
