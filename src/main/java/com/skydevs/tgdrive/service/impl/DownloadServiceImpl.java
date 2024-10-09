package com.skydevs.tgdrive.service.impl;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private BotService botService;

    /**
     * 下载文件
     * @param fileID
     * @return
     */
    @Override
    public ResponseEntity<Resource> downloadFile(String fileID) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            // 从 botService 获取文件的下载路径和文件名
            String fileUrl = botService.getFullDownloadPath(fileID);
            String filename = botService.getFileNameByID(fileID);

            // 使用 URL 获取文件
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 检查响应码
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 获取输入流
                inputStream = connection.getInputStream();

                // 将 InputStream 转换为字节数组
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = byteArrayOutputStream.toByteArray();

                // 创建 ByteArrayResource
                ByteArrayResource resource = new ByteArrayResource(fileBytes);

                // 设置响应头
                HttpHeaders headers = new HttpHeaders();

                // 根据文件扩展名设置 Content-Type
                String contentType = getContentTypeFromFilename(filename);
                headers.setContentType(MediaType.parseMediaType(contentType));

                // 如果是图片，不设置 Content-Disposition 以便浏览器直接显示
                if (!contentType.startsWith("image/")) {
                    headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
                }

                // 返回响应
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
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
}
