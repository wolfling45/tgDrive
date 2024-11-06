package com.skydevs.tgdrive.service.impl;

import com.alibaba.fastjson.JSON;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.DownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private BotService botService;
    @Autowired
    private FileMapper fileMapper;

    @Override
    public ResponseEntity<Resource> downloadFile(String fileID) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            // 从 botService 获取文件的下载路径和文件名
            String fileUrl = botService.getFullDownloadPath(fileID);
            String filename = fileMapper.getFileNameByFileId(fileID);
            if (filename == null || filename.isEmpty()) {
                filename = botService.getFileNameByID(fileID);
            }

            // 下载文件
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();

                // 将文件内容读取为字符串
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                String fileContent = byteArrayOutputStream.toString();

                // 尝试解析为 BigFileInfo 并检查 isRecordFile 标识
                try {
                    BigFileInfo record = JSON.parseObject(fileContent, BigFileInfo.class);

                    // 通过 isRecordFile 判断是否为记录文件
                    if (record.isRecordFile()) {
                        log.info("文件名为：" + record.getFileName());
                        log.info("检测到记录文件，开始下载并合并分片文件...");

                        // 开始下载每个分片文件并进行合并
                        ByteArrayOutputStream mergedOutputStream = new ByteArrayOutputStream();
                        for (String partFileId : record.getFileIds()) {
                            String partFileUrl = botService.getFullDownloadPath(partFileId);
                            URL partUrl = new URL(partFileUrl);
                            connection = (HttpURLConnection) partUrl.openConnection();
                            connection.setRequestMethod("GET");

                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                try (InputStream partInputStream = connection.getInputStream()) {
                                    while ((bytesRead = partInputStream.read(buffer)) != -1) {
                                        mergedOutputStream.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        }

                        // 将合并的文件字节转换为资源
                        byte[] mergedFileBytes = mergedOutputStream.toByteArray();
                        ByteArrayResource resource = new ByteArrayResource(mergedFileBytes);

                        // 设置响应头
                        HttpHeaders headers = new HttpHeaders();
                        String contentType = getContentTypeFromFilename(record.getFileName());
                        headers.setContentType(MediaType.parseMediaType(contentType));
                        // 使用 UTF-8 编码文件名，避免中文字符问题
                        String encodedFilename = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");

                        // 根据浏览器是否支持 RFC 5987 来决定使用哪个 Content-Disposition 头
                        if (!contentType.startsWith("image/")) {
                            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFilename;
                            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
                        }

                        // 返回合并后的文件
                        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
                    }
                } catch (Exception e) {
                    // 解析失败，说明这是普通文件，直接返回文件内容
                    log.info("文件不是记录文件，直接下载文件...");
                    byte[] fileBytes = byteArrayOutputStream.toByteArray();

                    // 创建 ByteArrayResource
                    ByteArrayResource resource = new ByteArrayResource(fileBytes);

                    // 设置响应头
                    HttpHeaders headers = new HttpHeaders();
                    String contentType = getContentTypeFromFilename(filename);
                    headers.setContentType(MediaType.parseMediaType(contentType));

                    // 如果是图片，不设置 Content-Disposition 以便浏览器直接显示
                    if (!contentType.startsWith("image/")) {
                        // 使用 URLEncoder 编码文件名，确保支持中文
                        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20");
                        String contentDisposition = "attachment; filename*=UTF-8''" + encodedFilename;
                        headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
                    }

                    // 返回响应
                    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } catch (IOException e) {
            log.error("下载文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            // 确保连接和输入流关闭，防止资源泄漏
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 默认情况下返回内部服务器错误，避免缺少返回语句的错误
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    private String getContentTypeFromFilename(String filename) {
        // 通过文件名的扩展名来推测 MIME 类型
        Path path = Paths.get(filename);
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            // 如果无法确定类型，返回 application/octet-stream
            return "application/octet-stream";
        }
    }
    //TODO: GIF直接显示
}
