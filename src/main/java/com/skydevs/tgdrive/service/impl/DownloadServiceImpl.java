package com.skydevs.tgdrive.service.impl;

import com.alibaba.fastjson.JSON;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.DownloadService;
import com.skydevs.tgdrive.utils.OkHttpClientFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private BotService botService;
    @Autowired
    private FileMapper fileMapper;

    private final OkHttpClient okHttpClient = OkHttpClientFactory.createClient();
    @Override
    public ResponseEntity<StreamingResponseBody> downloadFile(String fileID) {
        try {
            // 从 botService 获取文件的下载路径和文件名
            String fileUrl = botService.getFullDownloadPath(fileID);
            String filename = fileMapper.getFileNameByFileId(fileID);
            if (filename == null) {
                filename = botService.getFileNameByID(fileID);
            }
            // 上传到tg的gif会被转换为MP4
            if (filename.endsWith(".gif")) {
                filename = filename.substring(0, filename.length() - 4) + ".mp4";
            }
            if (filename == null || filename.isEmpty()) {
                filename = botService.getFileNameByID(fileID);
            }

            // 设置响应头
            HttpHeaders headers = setHeaders(filename);

            // 创建 OkHttp请求
            Request request = new Request.Builder()
                    .url(fileUrl)
                    .get()
                    .build();

            // 执行请求
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();

            // 请求失败
            if (!response.isSuccessful()) {
                log.error("无法下载文件，响应码：" + response.code());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.error("响应体为空");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            InputStream inputStream = responseBody.byteStream();

            // 尝试解析为 BigFileInfo 并检查 isRecordFile 标识
            boolean isRecordFile = false;
            BigFileInfo record = null;
            try {
                String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                record = JSON.parseObject(fileContent, BigFileInfo.class);
                isRecordFile = record.isRecordFile();
                // 重新获取输入流，因为 readAllBytes 已经读取了流
                responseBody.close();
            } catch (Exception e) {
                log.info("文件不是 BigFileInfo类型，作为普通文件处理");
            }

            if (isRecordFile && record != null) {
                log.info("文件名为：" + record.getFileName());
                log.info("检测到记录文件，开始下载并合并分片文件...");

                List<String> partFileIds = record.getFileIds();

                StreamingResponseBody streamingResponseBody = outputStream -> {
                    // 开始下载每个分片文件并进行合并
                    try {
                        for (String partFileId : partFileIds) {
                            try (InputStream partInputStream = downloadFileByte(partFileId).byteStream()){
                                byte[] buffer = new byte[4096];
                                int byteRead;
                                while ((byteRead = partInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, byteRead);
                                }
                            }catch (Exception e) {
                                log.info("文件下载终止");
                                log.info(e.getMessage(), e);
                            }
                        }
                    } catch (Exception e) {
                        log.error("分片文件合并失败: " + e.getMessage(), e);
                        throw e;
                    }
                };

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType(getContentTypeFromFilename(filename)))
                        .body(streamingResponseBody);
            } else {
                // 解析失败，说明这是普通文件，直接返回文件内容
                log.info("文件不是记录文件，直接下载文件...");

                StreamingResponseBody streamingResponseBody = outputStream -> {
                    try (InputStream is = downloadFileByte(fileID).byteStream()) {
                        byte[] buffer = new byte[4096];
                        int byteRead;
                        while ((byteRead = is.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, byteRead);
                        }
                    }catch (Exception e) {
                        log.info("文件下载终止");
                        log.info(e.getMessage(), e);
                    }
                };

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType(getContentTypeFromFilename(filename)))
                        .body(streamingResponseBody);
            }
        } catch (Exception e) {
            log.error("下载文件失败：" + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private HttpHeaders setHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        try {
            String contentType = getContentTypeFromFilename(filename);
            headers.setContentType(MediaType.parseMediaType(contentType));

            if (contentType.startsWith("image/") || contentType.startsWith("video/")) {
                // 对于图片和视频，设置 Content-Disposition 为 inline
                headers.setContentDisposition(ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build());
            } else {
                // 使用 URLEncoder 编码文件名，确保支持中文
                String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20");
                String contentDisposition = "attachment; filename*=UTF-8''" + encodedFilename;
                headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("不支持的编码");
        }
        return headers;
    }

    private ResponseBody downloadFileByte(String partFileId) throws IOException {
        String partFileUrl = botService.getFullDownloadPath(partFileId);
        Request partRequest = new Request.Builder()
                .url(partFileUrl)
                .get()
                .build();

        Response response = okHttpClient.newCall(partRequest).execute();
        if (!response.isSuccessful()) {
            log.error("无法下载分片文件，响应码：" + response.code());
            throw new IOException("无法下载分片文件，响应码：" + response.code());
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            log.error("分片响应体为空");
            throw new IOException("分片响应体为空");
        }

        return responseBody;
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
                case "mp4":
                    contentType = "video/mp4";
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