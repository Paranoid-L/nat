package com.gitee.leo_92.nat.client.net;

import com.gitee.leo_92.nat.client.handler.NatClientHandler;
import com.gitee.leo_92.nat.common.codec.NatMessageDecoder;
import com.gitee.leo_92.nat.common.codec.NatMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NatClient {
    private final static Logger logger = LoggerFactory.getLogger(NatClient.class);

    @Value("${nat.client.server_address}")
    private String serverAddress;

    @Value("${nat.client.server_port}")
    private int serverPort;

    @Value("${nat.client.password}")
    private String password;

    @Value("${nat.client.proxy_addresses}")
    private String proxyAddresses;

    @Value("${nat.client.remote_ports}")
    private String remotePorts;

    /**
     * 初始化注册映射连接端口
     *
     * @throws Exception 异常
     */
    public void startConnect() throws Exception {
        if (proxyAddresses.contains(",")) {
            String[] proxyAddressArrays = proxyAddresses.split(",");
            String[] remotePortArrays = remotePorts.split(",");
            if (proxyAddressArrays.length == remotePortArrays.length) {
                for (int i = 0; i < proxyAddressArrays.length; i++) {
                    String[] proxy = proxyAddressArrays[i].split(":");
                    int remotePort = Integer.parseInt(remotePortArrays[i]);
                    connectToServer(remotePort, proxy[0], Integer.parseInt(proxy[1]));
                }
            } else {
                logger.info("配置文件信息配置不正确！");
            }
        } else {
            String[] proxy = proxyAddresses.split(":");
            connectToServer(Integer.parseInt(remotePorts), proxy[0], Integer.parseInt(proxy[1]));
        }
    }

    /**
     * 注册映射端口连接server服务器
     *
     * @throws Exception 异常
     */
    public void connectToServer(int remotePort, String proxyAddress, int proxyPort) throws Exception {
        TcpConnection tcpConnection = new TcpConnection();
        tcpConnection.connect(serverAddress, serverPort, new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                NatClientHandler natClientHandler = new NatClientHandler(remotePort, password,
                        proxyAddress, proxyPort);
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                        new NatMessageDecoder(), new NatMessageEncoder(),
                        new IdleStateHandler(60, 30, 0), natClientHandler);
            }
        });
    }
}
