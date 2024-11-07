package com.skydevs.tgdrive.controller;

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
    public ResponseEntity<ConfigForm> getConfig(@RequestParam String name) {
        ConfigForm config = configService.get(name);
        if (config == null) {
            log.error("配置获取失败，请检查文件名是否错误");
            return null;
        }
        log.info("获取数据成功");
        return ResponseEntity.ok(config);
    }

    @PostMapping()
    public ResponseEntity<String> submitConfig(@RequestBody ConfigForm configForm) {
        configService.save(configForm);
        log.info("配置保存成功");
        return ResponseEntity.ok("配置已成功提交");
    }
}
