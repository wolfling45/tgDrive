package com.skydevs.tgdrive.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skydevs.tgdrive.config.AppConfig;
import com.skydevs.tgdrive.dto.ConfigForm;
import com.skydevs.tgdrive.exception.FileNotFoundException;
import com.skydevs.tgdrive.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Override
    public AppConfig get(String filename) {
        File configFile = new File("configJSON/" + filename + ".json");
        if (configFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(configFile.toString())));
                return JSON.parseObject(content, AppConfig.class);
            } catch (Exception e) {
                System.err.println("配置文件读取失败: " + e.getMessage());
                throw new RuntimeException("配置读取失败");
            }
        } else {
            throw new FileNotFoundException("文件不存在");
        }
    }

    @Override
    public void save(ConfigForm configForm) {
        try {
            String jsonString = JSON.toJSONString(configForm, true);
            Files.write(Paths.get("configJSON/" + configForm.getName() + ".json"),jsonString.getBytes());
        } catch (IOException e) {
            System.err.println("保存配置文件失败：" + e.getMessage());
        }
    }
}
