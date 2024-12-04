package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.result.PageResult;
import jakarta.servlet.http.HttpServletRequest;

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
}
