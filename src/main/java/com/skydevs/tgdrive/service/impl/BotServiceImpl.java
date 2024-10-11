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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@Slf4j
public class BotServiceImpl implements BotService {

    @Autowired
    private ConfigService configService;
    private String botToken;
    private String chatId;
    private String url;
    private TelegramBot bot;
    @Value("${server.port}")
    private int serverPort;


    /**
     * 设置基本配置
     * @param filename
     */
    public void setBotToken(String filename) {
        AppConfig appConfig = configService.get(filename);
        if (appConfig == null) {
            log.error("文件加载失败");
            return;
        }
        try {
            botToken = appConfig.getToken();
            chatId = appConfig.getTarget();
        } catch (Exception e) {
            log.error("获取Bot Token失败: {}", e.getMessage());
        }
        if (appConfig.getUrl() == null && appConfig.getUrl().isEmpty()) {
            url = "localhost:" + serverPort;
        } else {
            url = appConfig.getUrl();
        }
        bot = new TelegramBot(botToken);
    }

    //TODO 处理文件批量上传

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
            return url + "/d/" + fileID;
            //TODO 将文件的文件名、fileID、下载路径、filesize、大小、上传时间存入sqlite

        } catch (IOException e) {
            log.error("文件上传失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 获取完整下载路径
     * @param fileID
     * @return
     */
    public String getFullDownloadPath(String fileID) {
        GetFile getFile = new GetFile(fileID);
        GetFileResponse getFileResponse = bot.execute(getFile);

        File file = getFileResponse.file();
        return bot.getFullFilePath(file);
    }

    @Override
    public String getFileNameByID(String fileID) {
        GetFile getFile = new GetFile(fileID);
        GetFileResponse getFileResponse = bot.execute(getFile);
        File file = getFileResponse.file();
        return file.filePath();
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