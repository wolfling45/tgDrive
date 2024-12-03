package com.skydevs.tgdrive.service;

import com.pengrad.telegrambot.model.File;
import com.skydevs.tgdrive.dto.UploadFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

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
    boolean setBotToken(String filename);


    /**
     * 发送消息
     * @param message
     */
    boolean sendMessage(String message);


    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    UploadFile getUploadFile(MultipartFile multipartFile, HttpServletRequest request);

    /**
     * 获取完整下载路径
     * @param file
     * @return
     */
    String getFullDownloadPath(File file);


    /**
     * 根据fileId获取文件
     * @param fileId
     * @return
     */
    File getFile(String fileId);

    /**
     * 根据ID获取文件名
     * @param fileID
     * @return
     */
    String getFileNameByID(String fileID);

}
