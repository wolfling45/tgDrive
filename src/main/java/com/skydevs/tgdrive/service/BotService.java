package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.result.PageResult;
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
    String uploadFile(MultipartFile multipartFile, String prefix);

    /**
     * 获取完整下载路径
     * @param fileID
     * @return
     */
    String getFullDownloadPath(String fileID);

    /**
     * 根据ID获取文件名
     * @param fileID
     * @return
     */
    String getFileNameByID(String fileID);

    /**
     * 分页查询文件列表
     * @param page
     * @param size
     * @return
     */
    PageResult getFileList(int page, int size);
}
