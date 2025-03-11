package com.skydevs.tgdrive.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private static final String DATABASE_PATH = "db/tgDrive.db"; // SQLite 文件路径

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadBackup() throws IOException {
        File file = new File(DATABASE_PATH);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tgDrive.db")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
