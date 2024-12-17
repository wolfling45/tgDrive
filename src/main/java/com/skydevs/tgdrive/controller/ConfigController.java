package com.skydevs.tgdrive.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.skydevs.tgdrive.dto.ConfigForm;
import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.BotService;
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
    @Autowired
    private BotService botService;

    @SaCheckLogin
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

    /**
     * 提交配置文件
     * @param configForm
     * @return
     */
    @SaCheckLogin
    @PostMapping()
    public Result<String> submitConfig(@RequestBody ConfigForm configForm) {
        configService.save(configForm);
        log.info("配置保存成功");
        return Result.success("配置保存成功");
    }

    /**
     * 加载配置
     *
     * @param filename 配置文件名
     * @return
     */
    @SaCheckLogin
    @GetMapping("/{filename}")
    public Result<String> loadConfig(@PathVariable("filename") String filename) {
        if (botService.setBotToken(filename)) {
            log.info("加载配置成功");
            return Result.success("配置加载成功");
        } else {
            log.error("配置加载失败");
            return Result.error("配置加载失败");
        }
    }
}
