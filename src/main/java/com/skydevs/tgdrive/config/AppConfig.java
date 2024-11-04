package com.skydevs.tgdrive.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AppConfig {
    // bot token, can be used by command line or environment
    private String token;

    // Channel name or id
    private String target;

    // password
    private String pass;

    // basic url
    private String url;
}
