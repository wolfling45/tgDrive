package com.skydevs.tgdrive.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface DownloadService {
    /**
     * 下载文件
     * @param fileID
     * @return
     */
    ResponseEntity<Resource> downloadFile(String fileID);
}
