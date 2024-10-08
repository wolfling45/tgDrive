package com.skydevs.tgdrive.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.ConfigService;
import com.skydevs.tgdrive.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Service
@Slf4j
public class BotServiceImpl implements BotService {
    private TelegramClient telegramClient;

    @Autowired
    private ConfigService configService;
    private String botToken;
    private String chatId;

    @Autowired
    public BotServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    public void setBotToken(String filename) {
        try {
            AppConfig appConfig = configService.get(filename);
            botToken = appConfig.getToken();
            chatId = appConfig.getTarget();
        } catch (Exception e) {
            log.error("获取Bot Token失败: {}", e.getMessage());
        }
    }

    public void sendImageUploadingAFile(File file) {
        InputFile inputFile = new InputFile(file);
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, inputFile);
        try {
            // Execute the method
            telegramClient.execute(sendPhotoRequest);
            log.info("图片发送成功");
        } catch (TelegramApiException e) {
            log.info(e.getMessage());
        }
    }

    public void sendMessage(String m) {
        TelegramBot bot = new TelegramBot(botToken);
        bot.execute(new com.pengrad.telegrambot.request.SendMessage(chatId, m));
        log.info("消息发送成功");
    }

    @Override
    public synchronized void initializeTelegramClientAsync() {
        if (this.botToken != null && !this.botToken.isEmpty()) {
            try {
                telegramClient = new OkHttpTelegramClient(botToken);
                log.info("Telegram客户端初始化成功");
            } catch (Exception e) {
                log.error("无法初始化Telegram客户端: {}", e.getMessage());
                telegramClient = null;
            }
        } else {
            log.error("Bot Token为空，无法初始化Telegram客户端");
            telegramClient = null;
        }
    }


    @Override
    public void consume(Update update) {
        if (telegramClient == null) {
            log.error("Telegram客户端未初始化，无法处理更新");
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(messageText)
                    .build();
            try {
                telegramClient.execute(message);
                log.info("发送消息成功: {}", messageText);
            } catch (TelegramApiException e) {
                log.error("发送消息失败: {}", e.getMessage());
            } catch (Exception e) {
                log.error("未知错误，发送消息失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        if (botSession != null && botSession.isRunning()) {
            log.info("Registered bot running state is: true");
        } else {
            log.error("Bot注册失败或未运行");
        }
    }
}