package com.skydevs.tgdrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration;

@SpringBootApplication(exclude = {TelegramBotStarterConfiguration.class})
@EnableAsync
public class TgDriveApplication {
    public static void main(String[] args) {
       SpringApplication.run(TgDriveApplication.class, args);
    }

}
