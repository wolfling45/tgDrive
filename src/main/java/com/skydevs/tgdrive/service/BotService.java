package com.skydevs.tgdrive.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * 机器人服务类，用于初始化机器人和运行机器人相关服务
 */
public interface BotService extends LongPollingSingleThreadUpdateConsumer, SpringLongPollingBot {
    /**
     * 检查更新
     * @param update
     */
    @Override
    public void consume(Update update);
    /**
     * 获取bot token
     * @return
     */
    @Override
    public String getBotToken();

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer();

    /**
     * 根据文件名设置botToken
     * @param filename
     */
    void setBotToken(String filename);

    /**
     * 初始化telegram客户端
     */
    void initializeTelegramClientAsync();
}
