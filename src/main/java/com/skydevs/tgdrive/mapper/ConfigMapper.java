package com.skydevs.tgdrive.mapper;

import com.skydevs.tgdrive.dto.ConfigForm;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigMapper {
    void insert(ConfigForm configForm);
}
