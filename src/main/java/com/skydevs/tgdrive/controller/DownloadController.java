package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.service.DownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/d")
@Slf4j
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/{fileID}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable String fileID) {
        log.info("fileID: " + fileID);
        return downloadService.downloadFile(fileID);
    }

}
