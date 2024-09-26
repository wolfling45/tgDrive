package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.config.AppConfig;
import com.skydevs.tgdrive.dto.ConfigForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private AppConfig appConfig;

    @GetMapping()
    public ResponseEntity<AppConfig> getConfig() {
        return ResponseEntity.ok(appConfig);
    }

    @PostMapping()
    public ResponseEntity<String> submitConfig(@RequestBody ConfigForm configForm) {
        appConfig.setMode(configForm.getMode());
        appConfig.setPass(configForm.getPass());
        appConfig.setUrl(configForm.getUrl());
        appConfig.setToken(configForm.getToken());
        appConfig.setTarget(configForm.getTarget());

        System.out.println("Bot Token: " + appConfig.getToken());
        System.out.println("Target: " + appConfig.getTarget());

        return ResponseEntity.ok("配置已成功提交");
    }
}
