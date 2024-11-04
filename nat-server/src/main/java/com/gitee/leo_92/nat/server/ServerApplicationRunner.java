package com.gitee.leo_92.nat.server;

import com.gitee.leo_92.nat.server.net.NatServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
public class ServerApplicationRunner implements ApplicationRunner {
    @Autowired
    private NatServer natServer;

    @Override
    public void run(ApplicationArguments args) {
        try {
            natServer.startNatServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
