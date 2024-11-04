package com.skydevs.tgdrive.entity;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo {
    private Long id;                // 文件ID
    private String fileName;         // 文件名
    private String downloadUrl;      // 文件下载URL

    // 用于存储上传时间的 UNIX 时间戳
    private Long uploadTime;

    // 将 LocalDateTime 转换为 UNIX 时间戳
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime.toEpochSecond(ZoneOffset.UTC);
    }

    // 获取 UNIX 时间戳并转换回 LocalDateTime
    public LocalDateTime getUploadTime() {
        return LocalDateTime.ofEpochSecond(uploadTime, 0, ZoneOffset.UTC);
    }
}

