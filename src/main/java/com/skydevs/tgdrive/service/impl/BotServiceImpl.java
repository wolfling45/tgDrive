package com.skydevs.tgdrive.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import com.skydevs.tgdrive.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class BotServiceImpl implements BotService {

    @Autowired
    private ConfigService configService;
    private String botToken;
    private String chatId;
    private TelegramBot bot;


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
        bot = new TelegramBot(botToken);
    }


    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    @Override
    public String uploadFile(MultipartFile multipartFile) {
        try {
            // 获取文件的字节数组
            byte[] fileBytes = multipartFile.getBytes();

            // 使用 Telegram Bot 直接发送字节数组
            SendDocument sendDocument = new SendDocument(chatId, fileBytes)
                    .fileName(multipartFile.getOriginalFilename());  // 设置文档的文件名为标题（可选）

            SendResponse response = bot.execute(sendDocument);
            Message message = response.message();
            String fileID = message.document().fileId();

            log.info("File ID: " + fileID);
            return fileID;

        } catch (IOException e) {
            log.error("文件上传失败: " + e.getMessage());
        }

        return null;
    }


    private String getDownloadPath(String fileID) {


        // 获取文件下载路径
        GetFile fileRequest = new GetFile(fileID);
        GetFileResponse getFileResponse = bot.execute(fileRequest);
        File file = getFileResponse.file();
        String fullPath = bot.getFullFilePath(file);
        log.info(fullPath);
        log.info(file.filePath());
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