package com.skydevs.tgdrive.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        // 客户端中止连接，记录为信息级别日志或忽略
        log.info("客户端中止了连接：{}", e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        String message = e.getMessage();
        if (message != null && (message.contains("An established connection was aborted") || message.contains("你的主机中的软件中止了一个已建立的连接"))) {
            log.info("客户端中止了连接：{}", message);
        } else {
            // 处理其他 IOException
            log.error("发生了 IOException", e);
        }
    }
}


