package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.config.AppConfig;
import com.skydevs.tgdrive.dto.ConfigForm;

public interface ConfigService {

    AppConfig get(String filename);

    void save(ConfigForm configForm);
}
