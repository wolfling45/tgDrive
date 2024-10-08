package com.skydevs.tgdrive.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import com.skydevs.tgdrive.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class BotServiceImpl implements BotService {

    @Autowired
    private ConfigService configService;
    private String botToken;
    private String chatId;

    @Autowired
    public BotServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 设置bot token
     * @param filename
     */
    public void setBotToken(String filename) {
        try {
            AppConfig appConfig = configService.get(filename);
            botToken = appConfig.getToken();
            chatId = appConfig.getTarget();
        } catch (Exception e) {
            log.error("获取Bot Token失败: {}", e.getMessage());
        }
    }


    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    @Override
    public String uploadFile(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        TelegramBot bot = new TelegramBot(botToken);
        SendResponse response = bot.execute(new SendDocument(chatId, file));
        Message message = response.message();
        String fileID = message.document().fileId();
        log.info(fileID);
        return fileID;
    }

    /**
     * 发送消息
     * @param m
     */
    public void sendMessage(String m) {
        TelegramBot bot = new TelegramBot(botToken);
        bot.execute(new SendMessage(chatId, m));
        log.info("消息发送成功");
    }


    /**
     * 获取bot token
     * @return
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}