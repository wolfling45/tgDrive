package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.config.AppConfig;
import com.skydevs.tgdrive.dto.ConfigForm;
import com.skydevs.tgdrive.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping()
    public ResponseEntity<AppConfig> getConfig(@RequestParam String filename) {
        AppConfig appConfig = configService.get(filename);
        log.info("获取数据成功");
        return ResponseEntity.ok(appConfig);
    }

    @PostMapping()
    public ResponseEntity<String> submitConfig(@RequestBody ConfigForm configForm) {
        configService.save(configForm);
        log.info("配置保存成功");
        return ResponseEntity.ok("配置已成功提交");
    }
}
