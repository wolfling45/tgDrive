package com.skydevs.tgdrive.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo {
    private String fileName;         // 文件名
    private String downloadUrl;      // 文件下载URL
    private String size;
    private String fileId;

    // 用于存储上传时间的 UNIX 时间戳
    private Long uploadTime;

    // 获取 UNIX 时间戳并转换回 LocalDateTime
    public LocalDateTime getUploadTime() {
        return LocalDateTime.ofEpochSecond(uploadTime, 0, ZoneOffset.UTC);
    }
}

