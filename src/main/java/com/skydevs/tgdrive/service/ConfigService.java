package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.dto.ConfigForm;

public interface ConfigService {

    ConfigForm get(String filename);

    void save(ConfigForm configForm);
}
