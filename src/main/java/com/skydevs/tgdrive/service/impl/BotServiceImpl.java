package com.skydevs.tgdrive.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.dto.ConfigForm;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.entity.FileInfo;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.result.PageResult;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import com.skydevs.tgdrive.utils.UserFriendly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BotServiceImpl implements BotService {

    @Autowired
    private ConfigService configService;
    @Autowired
    private UserFriendly userFriendly;
    @Autowired
    private FileMapper fileMapper;
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
        ConfigForm config = configService.get(filename);
        if (config == null) {
            log.error("配置加载失败");
            return;
        }
        try {
            botToken = config.getToken();
            chatId = config.getTarget();
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

    private String sendFileBytes(byte[] fileBytes, String filename) {
        // 使用 Telegram Bot 直接发送字节数组
        SendDocument sendDocument = new SendDocument(chatId, fileBytes)
                .fileName(filename);// 设置文档的文件名

        try {
            SendResponse response = bot.execute(sendDocument);
            // 检查 response 是否成功
            if (response.isOk()) {
                Message message = response.message();
                String fileID;
                if (message.document() != null) {
                    fileID = message.document().fileId();
                    log.info("文件上传成功，File ID: " + fileID);
                    return fileID;
                } else if (message.sticker().fileId() != null) {
                    fileID = message.sticker().fileId();
                    log.info("文件上传成功，File ID: " + fileID);
                    return fileID;
                } else {
                    // 处理 message 或 document 为 null 的情况
                    log.error("sticker or document is null. Response: {}", response);
                }
            } else {
                // 处理 API 请求失败的情况
                log.error("Failed to send document. Error: {}", response.description());
            }
        } catch (RuntimeException e) {
            log.error("Failed to send document. network error");
        }
       return null;
    }

    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    //TODO: 改用多线程加速上传
    @Override
    public String uploadFile(MultipartFile multipartFile, String prefix) {
        try {
            // 判断文件大小是否大于 10MB
            if (multipartFile.getSize() > MAX_FILE_SIZE) {
                log.info("文件大于 10MB，开始切割并上传...{}", userFriendly.humanReadableFileSize(multipartFile.getSize()));

                // 将文件切割为小于等于 10MB 的部分
                List<java.io.File> fileParts = splitFile(multipartFile);

                // 保存每个部分的 file_id
                List<String> fileIds = new ArrayList<>();

                // 上传每个文件部分并保存 file_id
                int retryCount = 3;
                for (java.io.File part : fileParts) {
                    byte[] fileBytes = Files.readAllBytes(part.toPath());
                    String fileID = null;

                    try {
                        for (int i = 0; i < retryCount; i++) {
                            fileID = sendFileBytes(fileBytes, part.getName());
                            if (fileID != null) {
                                break;
                            }
                            log.warn("上传失败，正在重试第" + (i + 1) + "次");
                        }

                        if (fileID == null) {
                            log.error("分片上传失败，文件名: " + part.getName() + "，整个文件终止上传");
                            throw new RuntimeException("上传失败，终止上传流程。文件名: " + multipartFile.getOriginalFilename());
                        }
                        log.info("分片上传成功，File ID: " + fileID);
                        fileIds.add(fileID);
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                        return null;
                    } finally {
                        // 删除本地临时分片文件
                        part.delete();
                    }
                }
                // 创建一个记录文件，包含所有分片的 file_id 信息
                String record = createRecordFile(multipartFile.getOriginalFilename(), multipartFile.getSize(), fileIds);

                // 存入数据库
                FileInfo fileInfo = FileInfo.builder()
                        .fileId(record)
                        .size(userFriendly.humanReadableFileSize(multipartFile.getSize()))
                        .uploadTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                        .downloadUrl(prefix + "/d/" + record)
                        .fileName(multipartFile.getOriginalFilename())
                        .build();
                fileMapper.insertFile(fileInfo);
                return "/d/" + record; // 返回记录文件的下载路径
            } else {
                // 文件小于等于 10MB，直接上传
                byte[] fileBytes = multipartFile.getBytes();

                String fileID = null;
                fileID = sendFileBytes(fileBytes, multipartFile.getOriginalFilename());
                try {
                    if (fileID != null) {
                        // 存入数据库
                        FileInfo fileInfo = FileInfo.builder()
                                .fileId(fileID)
                                .size(userFriendly.humanReadableFileSize(multipartFile.getSize()))
                                .uploadTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                                .downloadUrl(prefix + "/d/" + fileID)
                                .fileName(multipartFile.getOriginalFilename())
                                .build();
                        fileMapper.insertFile(fileInfo);
                        return "/d/" + fileID;
                    } else {
                        throw new RuntimeException("文件上传失败");
                    }
                } catch (RuntimeException e) {
                    log.error(e.getMessage());
                }
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

        return recordFileId;
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

    @Override
    public PageResult getFileList(int page, int size) {
        // 设置分页
        PageHelper.startPage(page, size);
        Page<FileInfo> pageInfo = fileMapper.getAllFiles();
        List<FileInfo> fileInfos = new ArrayList<>();
        for (FileInfo fileInfo : pageInfo) {
            FileInfo fileInfo1 = new FileInfo();
            BeanUtils.copyProperties(fileInfo, fileInfo1);
            fileInfos.add(fileInfo1);
        }
        log.info("文件分页查询");
        return new PageResult((int) pageInfo.getTotal(), fileInfos);
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