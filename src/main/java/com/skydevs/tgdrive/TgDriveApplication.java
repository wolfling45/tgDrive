package com.skydevs.tgdrive;

import com.skydevs.tgdrive.Config.AppConfig;
import com.skydevs.tgdrive.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TgDriveApplication {
    public static void main(String[] args) {
       SpringApplication.run(TgDriveApplication.class, args);
    }

}
