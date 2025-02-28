package com.skydevs.tgdrive.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.dto.ConfigForm;
import com.skydevs.tgdrive.dto.UploadFile;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.entity.FileInfo;
import com.skydevs.tgdrive.exception.*;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import com.skydevs.tgdrive.utils.StringUtil;
import com.skydevs.tgdrive.utils.UserFriendly;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
public class BotServiceImpl implements BotService {

    @Autowired
    private ConfigService configService;
    @Autowired
    private FileMapper fileMapper;
    private String botToken;
    private String chatId;
    private TelegramBot bot;
    // 控制同时运行的任务数量
    private final int PERMITS = 5;
    // tg bot接口限制20MB，传10MB是最佳实践
    private final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    /*
    @Value("${server.port}")
    private int serverPort;
    private String url;
     */


    /**
     * 设置基本配置
     *
     * @param filename 配置文件名
     */
    public void setBotToken(String filename) {
        ConfigForm config = configService.get(filename);
        if (config == null) {
            log.error("配置文件不存在");
            throw new ConfigFileNotFoundException();
        }
        try {
            botToken = config.getToken();
            chatId = config.getTarget();
        } catch (Exception e) {
            log.error("获取Bot Token失败: {}", e.getMessage());
            throw new GetBotTokenFailedException();
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
     * 分块上传文件
     *
     * @param inputStream
     * @param filename
     * @return
     */
    private List<String> sendFileStreamInChunks(InputStream inputStream, String filename) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(PERMITS); // 线程池大小
        Semaphore semaphore = new Semaphore(PERMITS); // 控制同时运行的任务数量

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[MAX_FILE_SIZE]; // 10MB 缓冲区
            int partIndex = 0;

            while (true) {
                // 用offset追踪buffer读了多少字节
                int offset = 0;
                while(offset < MAX_FILE_SIZE) {
                    int byteRead = bufferedInputStream.read(buffer, offset, MAX_FILE_SIZE - offset);
                    if (byteRead == -1) {
                        break;
                    }
                    offset += byteRead;
                }

                if (offset == 0) {
                    break;
                }
                semaphore.acquire(); // 获取许可，若没有可用许可则阻塞

                // 当前块的文件名
                String partName = filename + "_part" + partIndex;
                partIndex++;

                // 取当前分块数据
                byte[] chunkData = Arrays.copyOf(buffer, offset);

                // 提交上传任务，使用CompletableFuture
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String fileId = uploadChunk(chunkData, partName);
                        if (fileId == null) {
                            throw new RuntimeException("分块 " + partName + " 上传失败");
                        }
                        return fileId;
                    } finally {
                        semaphore.release(); // 在任务完成后释放信号量
                    }
                }, executorService);
                futures.add(future);
            }

            // 等待所有任务完成并按顺序获取结果
            List<String> fileIds = new ArrayList<>();
            try {
                for (CompletableFuture<String> future : futures) {
                    fileIds.add(future.join()); // 按顺序等待结果
                }
                return fileIds;
            } catch (CompletionException e) {
                for (CompletableFuture<String> future : futures) {
                    future.cancel(true);
                }
                executorService.shutdown();
                throw new RuntimeException("分块上传失败: " + e.getCause().getMessage(), e);
            }
        } catch (IOException | InterruptedException e) {
            log.error("文件流读取失败或上传失败：{}", e.getMessage());
            throw new RuntimeException("文件流读取失败或上传");
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * 上传块
     *
     * @param chunkData
     * @param partName
     * @return
     * @throws EOFException
     */
    private String uploadChunk(byte[] chunkData, String partName) {
        SendDocument sendDocument = new SendDocument(chatId, chunkData).fileName(partName);
        try {
            SendResponse response = bot.execute(sendDocument);

            int retryCount = 3;
            for (int i = 1; i <= retryCount; i++) {
                // 检查响应
                if (response.isOk() && response.message() != null && (response.message().document() != null || response.message().sticker() != null)) {
                    String fileID = response.message().document().fileId() != null ? response.message().document().fileId() : response.message().sticker().fileId();
                    log.info("分块上传成功，File ID：{}， 文件名：{}", fileID, partName);
                    return fileID;
                } else {
                    log.warn("正在重试第" + i + "次");
                    response = bot.execute(sendDocument);
                }
            }
            throw new NoConnectionException();
        }catch (NullPointerException e) {
            throw new BotNotSetException();
        }
    }

    /**
     * 上传单文件（为了使gif能正常显示，gif上传到tg后，会被转换为MP4）
     * @param inputStream
     * @param filename
     * @return
     */
    private String uploadOneFile(InputStream inputStream, String filename) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int byteRead;
            while ((byteRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, byteRead);
            }
            byte[] chunkData = buffer.toByteArray();
            return uploadChunk(chunkData, filename);
        } catch (IOException e) {
            log.error("文件上传失败 :" + e.getMessage());
            return null;
        }
    }

    /**
     * 生成上传文件
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @Override
    public UploadFile getUploadFile(MultipartFile multipartFile, HttpServletRequest request) {
        UploadFile uploadFile = new UploadFile();
        if (!multipartFile.isEmpty()) {
            String downloadUrl = uploadFile(multipartFile, request);
            uploadFile.setFileName(multipartFile.getOriginalFilename());
            uploadFile.setDownloadLink(downloadUrl);
        } else {
            uploadFile.setFileName("文件不存在");
        }

        return uploadFile;
    }

    /**
     * 上传文件
     *
     * @param multipartFile
     * @param request
     * @return 文件下载地址
     */
    private String uploadFile(MultipartFile multipartFile, HttpServletRequest request) {
        try {
            String prefix = StringUtil.getPrefix(request);
            InputStream inputStream = multipartFile.getInputStream();
            String filename = multipartFile.getOriginalFilename();
            long size = multipartFile.getSize();
            if (size > MAX_FILE_SIZE) {
                List<String> fileIds = sendFileStreamInChunks(inputStream, filename);
                String fileID = createRecordFile(filename, size, fileIds);
                FileInfo fileInfo = FileInfo.builder()
                        .fileId(fileID)
                        .size(UserFriendly.humanReadableFileSize(size))
                        .fullSize(size)
                        .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                        .downloadUrl(prefix + "/d/" + fileID)
                        .fileName(filename)
                        .build();
                fileMapper.insertFile(fileInfo);
                return prefix + "/d/" + fileID;
            } else {
                // 小于10MB的GIF会被TG转换为MP4，对文件后缀进行处理
                String uploadFilename = filename;
                if (filename != null && filename.endsWith(".gif")) {
                    uploadFilename = filename.substring(0, filename.lastIndexOf(".gif"));
                }
                String fileID = uploadOneFile(inputStream, uploadFilename);
                FileInfo fileInfo = FileInfo.builder()
                        .fileId(fileID)
                        .size(UserFriendly.humanReadableFileSize(size))
                        .fullSize(size)
                        .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                        .downloadUrl(prefix + "/d/" + fileID)
                        .fileName(filename)
                        .build();
                fileMapper.insertFile(fileInfo);
                return prefix + "/d/" + fileID;
            }
        } catch (IOException e) {
            log.error("文件上传失败，响应信息：{}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 生成recordFile
     *
     * @param originalFileName
     * @param fileSize
     * @param fileIds
     * @return
     * @throws IOException
     */
    private String createRecordFile(String originalFileName, long fileSize, List<String> fileIds) throws IOException {
        BigFileInfo record = new BigFileInfo();
        record.setFileName(originalFileName);
        record.setFileSize(fileSize);
        record.setFileIds(fileIds);
        record.setRecordFile(true);

        // 创建一个系统临时文件，不依赖特定路径
        Path tempDir = Files.createTempDirectory("tempDir");
        String hashString = DigestUtil.sha256Hex(originalFileName);
        Path tempFile = tempDir.resolve(hashString + ".record.json");
        Files.createFile(tempFile);
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
     * 获取完整下载路径
     *
     * @param file
     * @return
     */
    public String getFullDownloadPath(File file) {
        log.info("获取完整的下载路径: " + bot.getFullFilePath(file));
        return bot.getFullFilePath(file);
    }

    /**
     * 根据fileId获取文件
     *
     * @param fileId
     * @return
     */
    public File getFile(String fileId) {
        GetFile getFile = new GetFile(fileId);
        try {
            GetFileResponse getFileResponse = bot.execute(getFile);
            return getFileResponse.file();
        } catch (NullPointerException e) {
            log.error("当前未加载配置文件！" + e.getMessage());
            throw new NoConfigException("当前未加载配置文件！");
        }
    }

    /**
     * 根据文件id获取文件名
     *
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
     *
     * @param m
     */
    public boolean sendMessage(String m) {
        TelegramBot bot = new TelegramBot(botToken);
        try {
            bot.execute(new SendMessage(chatId, m));
        } catch (Exception e) {
            log.error("消息发送失败", e);
            return false;
        }
        log.info("消息发送成功");
        return true;
    }


    /**
     * 获取bot token
     *
     * @return
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param path 文件路径
     * @return
     */
    @Override
    public String uploadFile(InputStream inputStream, String path) {
        try {
            String filename = path.substring(path.lastIndexOf('/') + 1);
            long size = inputStream.available();

            return getUploadedFileID(inputStream, filename, size);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }


    @Override
    public String uploadFile(InputStream inputStream, String path, HttpServletRequest request) {
        try {
            String filename = path.substring(path.lastIndexOf('/') + 1);
            long size = request.getContentLengthLong();

            return getUploadedFileID(inputStream, filename, size);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Nullable
    private String getUploadedFileID(InputStream inputStream, String filename, long size) throws IOException {
        if (size > MAX_FILE_SIZE) {
            List<String> fileIds = sendFileStreamInChunks(inputStream, filename);
            String fileID = createRecordFile(filename, size, fileIds);
            return fileID;
        } else {
            String uploadFilename = filename;
            if (filename.endsWith(".gif")) {
                uploadFilename = filename.substring(0, filename.lastIndexOf(".gif"));
            }
            return uploadOneFile(inputStream, uploadFilename);
        }
    }

    @Override
    public InputStream downloadFile(String fileId) {
        try {
            File file = getFile(fileId);
            String fileUrl = bot.getFullFilePath(file);
            return new URL(fileUrl).openStream();
        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public void deleteFile(String fileId) {
        try {
            bot.execute(new DeleteMessage(chatId, Integer.parseInt(fileId)));
            log.info("文件删除成功，File ID: {}", fileId);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败", e);
        }
    }
}