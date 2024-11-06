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
import java.io.UnsupportedEncodingException;
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

    //TODO: 改为流式传输
    @Override
    public ResponseEntity<Resource> downloadFile(String fileID) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            // 从 botService 获取文件的下载路径和文件名
            String fileUrl = botService.getFullDownloadPath(fileID);
            String filename = fileMapper.getFileNameByFileId(fileID);
            if (filename.endsWith(".gif")) {
                filename = filename.substring(0, filename.length() - 4) + ".mp4";
            }
            if (filename == null || filename.isEmpty()) {
                filename = botService.getFileNameByID(fileID);
            }

            // 下载文件
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();

                // 将文件内容读取为字节数组
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = byteArrayOutputStream.toByteArray();

                // 尝试解析为 BigFileInfo 并检查 isRecordFile 标识
                boolean isRecordFile = false;
                BigFileInfo record = null;
                try {
                    String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
                    record = JSON.parseObject(fileContent, BigFileInfo.class);
                    isRecordFile = record.isRecordFile();
                } catch (Exception e) {
                    // 忽略异常，继续处理
                }

                if (isRecordFile && record != null) {
                    log.info("文件名为：" + record.getFileName());
                    log.info("检测到记录文件，开始下载并合并分片文件...");

                    // 开始下载每个分片文件并进行合并
                    ByteArrayOutputStream mergedOutputStream = new ByteArrayOutputStream();
                    for (String partFileId : record.getFileIds()) {
                        String partFileUrl = botService.getFullDownloadPath(partFileId);
                        URL partUrl = new URL(partFileUrl);
                        HttpURLConnection partConnection = null;
                        InputStream partInputStream = null;
                        try {
                            partConnection = (HttpURLConnection) partUrl.openConnection();
                            partConnection.setRequestMethod("GET");

                            if (partConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                partInputStream = partConnection.getInputStream();
                                while ((bytesRead = partInputStream.read(buffer)) != -1) {
                                    mergedOutputStream.write(buffer, 0, bytesRead);
                                }
                            } else {
                                log.error("无法下载分片文件，响应码：" + partConnection.getResponseCode());
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                            }
                        } finally {
                            if (partInputStream != null) {
                                partInputStream.close();
                            }
                            if (partConnection != null) {
                                partConnection.disconnect();
                            }
                        }
                    }

                    // 将合并的文件字节转换为资源
                    byte[] mergedFileBytes = mergedOutputStream.toByteArray();
                    ByteArrayResource resource = new ByteArrayResource(mergedFileBytes);

                    // 设置响应头
                    HttpHeaders headers = setHeaders(record.getFileName());

                   // 返回合并后的文件
                    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
                } else {
                    // 解析失败，说明这是普通文件，直接返回文件内容
                    log.info("文件不是记录文件，直接下载文件...");

                    // 创建 ByteArrayResource
                    ByteArrayResource resource = new ByteArrayResource(fileBytes);

                    // 设置响应头
                    HttpHeaders headers = setHeaders(filename);

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
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败: " + e.getMessage());
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpHeaders setHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        try {
            String contentType = getContentTypeFromFilename(filename);
            headers.setContentType(MediaType.parseMediaType(contentType));

            if (!contentType.startsWith("image/") || contentType.startsWith("image/gif")) {
                // 使用 URLEncoder 编码文件名，确保支持中文
                String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20");
                String contentDisposition = "attachment; filename*=UTF-8''" + encodedFilename;
                headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            } else {
                // 对于图片，设置 Content-Disposition 为 inline
                headers.setContentDisposition(ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build());
            }
        } catch (UnsupportedEncodingException e) {
            log.error("不支持的编码");
        }
        return headers;
    }

    private String getContentTypeFromFilename(String filename) {
        String contentType = null;
        Path path = Paths.get(filename);
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            log.warn("无法通过 Files.probeContentType 获取 MIME 类型: " + e.getMessage());
        }

        if (contentType == null) {
            // 手动映射常见的文件扩展名到 MIME 类型
            String extension = getFileExtension(filename).toLowerCase();
            switch (extension) {
                case "gif":
                    contentType = "image/gif";
                    break;
                case "jpg":
                case "jpeg":
                    contentType = "image/jpeg";
                    break;
                case "png":
                    contentType = "image/png";
                    break;
                case "bmp":
                    contentType = "image/bmp";
                    break;
                case "txt":
                    contentType = "text/plain";
                    break;
                case "pdf":
                    contentType = "application/pdf";
                    break;
                // 添加其他需要的类型
                default:
                    contentType = "application/octet-stream";
                    break;
            }
        }

        return contentType;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
