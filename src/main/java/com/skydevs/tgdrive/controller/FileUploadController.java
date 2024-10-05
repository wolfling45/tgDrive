package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok("加载配置成功");
    }
}
