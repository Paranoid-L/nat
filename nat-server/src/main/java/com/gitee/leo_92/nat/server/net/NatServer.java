package com.gitee.leo_92.nat.server.net;

import com.gitee.leo_92.nat.common.codec.NatMessageDecoder;
import com.gitee.leo_92.nat.common.codec.NatMessageEncoder;
import com.gitee.leo_92.nat.server.handler.NatServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NatServer {
    @Value("${nat.server.hostname}")
    private String hostname;

    @Value("${nat.server.port}")
    private int port;

    @Value("${nat.server.password}")
    private String password;

    public void startNatServer() throws Exception {
        TcpServer tcpServer = new TcpServer();
        tcpServer.bind(port, new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                NatServerHandler natServerHandler = new NatServerHandler(password);
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                        new NatMessageDecoder(), new NatMessageEncoder(),
                        new IdleStateHandler(60, 30, 0), natServerHandler);
            }
        });
        System.out.println("Nat server started on port " + port);
    }
}
