package com.skydevs.tgdrive.mapper;

import com.skydevs.tgdrive.entity.FileInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileMapper {

    /**
     * 插入已上传文件
     * @param fileInfo
     */
    @Insert("INSERT INTO files (file_name, download_url, upload_time, file_id, size) VALUES (#{fileName}, #{downloadUrl}, #{uploadTime}, #{fileId}, #{size})")
    void insertFile(FileInfo fileInfo);

    /**
     * 获取全部文件
     * @return
     */
    @Select("SELECT * FROM files")
    List<FileInfo> getAllFiles();
}
