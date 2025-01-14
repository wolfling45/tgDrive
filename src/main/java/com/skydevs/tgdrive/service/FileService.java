package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.result.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public interface FileService {
    /**
     * 分页查询文件列表
     * @param page
     * @param size
     * @return
     */
    PageResult getFileList(int page, int size);

    /**
     * 更新文件url
     * @return
     */
    void updateUrl(HttpServletRequest request);

    /**
     * 上传文件到Telegram
     *
     * @param inputStream 文件输入流
     * @param request
     * @return 文件ID
     */
    String uploadToTelegram(InputStream inputStream, HttpServletRequest request);

    /**
     * 从Telegram下载文件
     * @param path 文件路径
     * @return 文件流
     */
    Optional<StreamingResponseBody> downloadFromTelegram(String path);

    /**
     * 从Telegram删除文件
     * @param path 文件路径
     */
    void deleteFromTelegram(String path);

    /**
     * 获取文件列表
     * @param path 路径
     * @return 文件列表
     */
    Map<String, Object> listFiles(String path);
}
