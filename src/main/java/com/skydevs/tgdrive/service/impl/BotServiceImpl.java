package com.skydevs.tgdrive.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import com.skydevs.tgdrive.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BotServiceImpl implements BotService {

    @Autowired
    private ConfigService configService;
    private String botToken;
    private String chatId;
    private TelegramBot bot;
    private final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    /*
    @Value("${server.port}")
    private int serverPort;
    private String url;
     */


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
        /*
        if (appConfig.getUrl() == null || appConfig.getUrl().isEmpty()) {
            url = "http://localhost:" + serverPort;
        } else {
            url = appConfig.getUrl();
        }
         */
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
            // 判断文件大小是否大于 10MB
            if (multipartFile.getSize() > MAX_FILE_SIZE) {
                log.info("文件大于 10MB，开始切割并上传...");

                // 将文件切割为小于等于 20MB 的部分
                List<java.io.File> fileParts = splitFile(multipartFile);

                // 保存每个部分的 file_id
                List<String> fileIds = new ArrayList<>();

                // 上传每个文件部分并保存 file_id
                for (java.io.File part : fileParts) {
                    byte[] fileBytes = Files.readAllBytes(part.toPath());

                    // 使用 Telegram Bot 发送每个文件部分
                    SendDocument sendDocument = new SendDocument(chatId, fileBytes)
                            .fileName(part.getName()); // 设置分片文件名

                    SendResponse response = bot.execute(sendDocument);
                    Message message = response.message();
                    String fileID = message.document().fileId();

                    log.info("分片上传成功，File ID: " + fileID);
                    fileIds.add(fileID);

                    // 删除本地临时分片文件
                    part.delete();
                }

                // 创建一个记录文件，包含所有分片的 file_id 信息
                String record = createRecordFile(multipartFile.getOriginalFilename(), multipartFile.getSize(), fileIds);

                return record; // 返回记录文件的 URL 或 file_id

            } else {
                // 文件小于等于 20MB，直接上传
                byte[] fileBytes = multipartFile.getBytes();

                // 使用 Telegram Bot 直接发送字节数组
                SendDocument sendDocument = new SendDocument(chatId, fileBytes)
                        .fileName(multipartFile.getOriginalFilename());  // 设置文档的文件名

                SendResponse response = bot.execute(sendDocument);
                Message message = response.message();
                String fileID = message.document().fileId();

                log.info("文件上传成功，File ID: " + fileID);
                return "/d/" + fileID;

                //TODO 将文件的文件名、fileID、下载路径、filesize、大小、上传时间存入sqlite
            }

        } catch (IOException e) {
            log.error("文件上传失败: " + e.getMessage());
        }

        return null;
    }


    /**
     * 生成上传文件
     * @param originalFileName
     * @param fileSize
     * @param fileIds
     * @return
     * @throws IOException
     */
    public String createRecordFile(String originalFileName, long fileSize, List<String> fileIds) throws IOException {
        BigFileInfo record = new BigFileInfo();
        record.setFileName(originalFileName);
        record.setFileSize(fileSize);
        record.setFileIds(fileIds);
        record.setRecordFile(true);

        // 创建一个系统临时文件，不依赖特定路径
        Path tempFile = Files.createTempFile(originalFileName + ".record", ".json");
        try {
            String jsonString = JSON.toJSONString(record, true);
            Files.write(Paths.get(tempFile.toUri()), jsonString.getBytes());
        } catch (IOException e) {
            log.error("上传记录文件生成失败" + e.getMessage());
            throw new RuntimeException("上传文件生成失败");
        }

        // 上传记录文件到 Telegram
        byte[] fileBytes = Files.readAllBytes(tempFile);
        SendDocument sendDocument = new SendDocument(chatId, fileBytes)
                .fileName(tempFile.getFileName().toString());

        SendResponse response = bot.execute(sendDocument);
        Message message = response.message();
        String recordFileId = message.document().fileId();

        log.info("记录文件上传成功，File ID: " + recordFileId);

        // 删除本地临时文件
        Files.deleteIfExists(tempFile);

        return "/d/" + recordFileId;
    }


    /**
     * 拆分大文件
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public List<java.io.File> splitFile(MultipartFile multipartFile) throws IOException {
        List<java.io.File> parts = new ArrayList<>();

        // 将 MultipartFile 转为本地文件
        java.io.File tempFile = java.io.File.createTempFile("temp", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        byte[] buffer = new byte[MAX_FILE_SIZE];
        int partNumber = 1;

        try (FileInputStream fis = new FileInputStream(tempFile)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                java.io.File partFile = new java.io.File(tempFile.getParent(), tempFile.getName() + ".part" + partNumber++);
                try (FileOutputStream fos = new FileOutputStream(partFile)) {
                    fos.write(buffer, 0, bytesRead);
                    parts.add(partFile);
                }
            }
        }

        // 删除临时文件
        tempFile.delete();

        return parts;
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

    /**
     * 根据文件id获取文件名
     * @param fileID
     * @return
     */
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