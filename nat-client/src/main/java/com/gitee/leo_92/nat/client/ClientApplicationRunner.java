package com.gitee.leo_92.nat.client;

import com.gitee.leo_92.nat.client.net.NatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
public class ClientApplicationRunner implements ApplicationRunner {
    private final NatClient natClient;

    /**
     * 定义初始化变量显示程序是否处于连接状态
     */
    public static boolean allowConnect = true;

    public ClientApplicationRunner(NatClient natClient) {
        this.natClient = natClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (allowConnect) {
                natClient.startConnect();
                // 已连接将状态置为不允许连接
                allowConnect = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置定时任务，每10分钟判断连接是否断开，断开状态则重连
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    private void eachTenMinuteExecute() {
        try {
            if (allowConnect) {
                natClient.startConnect();
                // 已连接将状态置为不允许连接
                allowConnect = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
