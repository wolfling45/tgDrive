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


@RestController
@Slf4j
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private BotService botService;

    @Autowired
    private ConfigService configService;

    /**
     * 加载配置
     * @param filename
     * @return
     */
    @GetMapping("/config/{filename}")
    public ResponseEntity<String> loadConfig(@PathVariable("filename") String filename) {
        botService.setBotToken(filename);
        log.info("加载配置成功");
        return ResponseEntity.ok("加载配置成功");
    }

    /**
     * 上传文件
     * @param multipartFiles
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile[] multipartFiles) {
        if (multipartFiles.length == 0 || multipartFiles == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("上传的文件为空");
        }

        StringBuilder resultMessage = new StringBuilder();

        for (MultipartFile file : multipartFiles) {
            if (!file.isEmpty()) {
                String downloadPath = botService.uploadFile(file);
                resultMessage.append("文件上传成功！文件名：")
                        .append(file.getOriginalFilename())
                        .append("，下载路径：")
                        .append(downloadPath)
                        .append("\n");
            } else {
                resultMessage.append("文件上传失败！文件名：")
                        .append(file.getOriginalFilename())
                        .append("为空。\n");
            }
        }

        return ResponseEntity.ok(resultMessage.toString());

    }

    /**
     * 发送消息
     * @param message
     * @return
     */
    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody Message message){
        log.info("处理消息发送");
        botService.sendMessage(message.getMessage());
        return ResponseEntity.ok("发送成功");
    }
}
