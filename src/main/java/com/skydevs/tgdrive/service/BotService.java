package com.skydevs.tgdrive.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 机器人服务类，用于初始化机器人和运行机器人相关服务
 */
@Service
public class BotService {
    /**
     * start bot
     */
    public void startBot() {
        Thread botThread = new Thread(() -> {
            BotDo();
        });
        botThread.setDaemon(true);
        botThread.start();
    }

    /**
     * 模拟机器人任务
     */
    public void BotDo() {
        //TODO 机器人的具体逻辑，链接tg机器人api，监听消息
        while(true) {
            try {
                //TODO 机器人活动
                System.out.println("机器人正在运行");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("机器人线程中断");
                break;
            }
        }
    }
}
