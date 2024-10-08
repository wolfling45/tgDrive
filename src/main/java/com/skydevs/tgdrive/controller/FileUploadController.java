package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.dto.Message;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private BotService botService;

    @Autowired
    private ConfigService configService;

    @GetMapping("/config/{filename}")
    public ResponseEntity<String> loadConfig(@PathVariable("filename") String filename) {
        botService.setBotToken(filename);
        botService.initializeTelegramClientAsync();
        log.info("加载配置成功");
        return ResponseEntity.ok("加载配置成功");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("上传的文件为空");
        }

        File file = new File(multipartFile.getName());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            log.info(e.getMessage());
        }

        botService.sendImageUploadingAFile(file);
        return ResponseEntity.ok("文件上传成功");
    }

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody Message message){
        log.info("处理消息发送");
        botService.sendMessage(message.getMessage());
        return ResponseEntity.ok("发送成功");
    }
}
