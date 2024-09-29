package com.skydevs.tgdrive.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 机器人服务类，用于初始化机器人和运行机器人相关服务
 */
public interface BotService {
    /**
     * start bot
     */
    void startBot();

    /**
     * 模拟机器人任务
     */
    void BotDo();
}
