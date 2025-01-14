package com.skydevs.tgdrive.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.skydevs.tgdrive.entity.FileInfo;
import com.skydevs.tgdrive.exception.FailedToGetSizeException;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.result.PageResult;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.FileService;
import com.skydevs.tgdrive.utils.UserFriendly;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private BotService botService;

    /**
     * 获取文件分页
     * @param page
     * @param size
     * @return
     */
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
     * 更新文件url
     * @return
     */
    @Override
    public void updateUrl(HttpServletRequest request) {
        String prefix = botService.getPrefix(request);
        fileMapper.updateUrl(prefix);
    }

    @Override
    public String uploadToTelegram(InputStream inputStream, HttpServletRequest request) {
        try {
            String path = request.getRequestURI().substring("/webdav".length());
            long size = request.getContentLengthLong();
            if (size < 0) {
                log.error("无法获取文件大小");
                throw new FailedToGetSizeException();
            }
            String fileId = botService.uploadFile(inputStream, path);
            // 从路径中提取文件名
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            FileInfo fileInfo = FileInfo.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .fullSize(size)
                    .size(UserFriendly.humanReadableFileSize(size))
                    .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                    .downloadUrl(botService.getPrefix(request) + "/d/" + fileId)
                    .webdavPath(path).build();
            fileMapper.insertFile(fileInfo);
            return fileId;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public Optional<StreamingResponseBody> downloadFromTelegram(String path) {
        try {
            FileInfo fileInfo = fileMapper.getFileByWebdavPath(path);
            if (fileInfo == null) {
                return Optional.empty();
            }
            InputStream inputStream = botService.downloadFile(fileInfo.getFileId());
            StreamingResponseBody responseBody = outputStream -> {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            };
            return Optional.of(responseBody);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteFromTelegram(String path) {
        try {
            FileInfo fileInfo = fileMapper.getFileByWebdavPath(path);
            if (fileInfo != null) {
                botService.deleteFile(fileInfo.getFileId());
                fileMapper.deleteFile(fileInfo.getFileId());
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public Map<String, Object> listFiles(String path) {
        try {
            List<FileInfo> files = fileMapper.getFilesByPathPrefix(path);
            Map<String, Object> result = new HashMap<>();
            result.put("files", files.stream()
                .map(file -> Map.of(
                    "name", file.getFileName(),
                    "size", file.getSize(),
                    "modified", file.getUploadTime()
                ))
                .collect(Collectors.toList()));
            return result;
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            throw new RuntimeException("获取文件列表失败", e);
        }
    }
}
