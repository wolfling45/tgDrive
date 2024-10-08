package com.skydevs.tgdrive.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

/**
 * 机器人服务类，用于初始化机器人和运行机器人相关服务
 */
public interface BotService{

    /**
     * 获取bot token
     * @return
     */
    public String getBotToken();


    /**
     * 根据文件名设置botToken
     * @param filename
     */
    void setBotToken(String filename);


    /**
     * 发送消息
     * @param message
     */
    void sendMessage(String message);


    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    String uploadFile(MultipartFile multipartFile);
}
