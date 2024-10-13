package com.skydevs.tgdrive.controller;

import com.alibaba.fastjson.JSON;
import com.skydevs.tgdrive.dto.Message;
import com.skydevs.tgdrive.dto.UploadFile;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


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

        List<UploadFile> uploadFiles = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            UploadFile uploadFile = new UploadFile();
            if (!file.isEmpty()) {
                String downloadPath = botService.uploadFile(file);
                uploadFile.setFileName(file.getOriginalFilename());
                uploadFile.setDownloadLink(downloadPath);
                uploadFiles.add(uploadFile);
            } else {
                uploadFile.setFileName("文件不存在");
            }
        }

        String resultJSON = JSON.toJSONString(uploadFiles);
        return ResponseEntity.ok(resultJSON);

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
