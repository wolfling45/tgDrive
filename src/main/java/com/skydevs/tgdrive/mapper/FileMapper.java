package com.skydevs.tgdrive.mapper;

import com.github.pagehelper.Page;
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
    @Insert("INSERT INTO files (file_name, download_url, upload_time, file_id, size, full_size, webdav_path) VALUES (#{fileName}, #{downloadUrl}, #{uploadTime}, #{fileId}, #{size}, #{fullSize}, #{webdavPath})")
    void insertFile(FileInfo fileInfo);

    /**
     * 获取全部文件
     * @return
     */
    @Select("SELECT * FROM files order by upload_time desc ")
    Page<FileInfo> getAllFiles();

    @Select("SELECT file_name FROM files where file_id = #{fileId}")
    String getFileNameByFileId(String fileId);

    @Select("SELECT full_size FROM files where file_id = #{fileId}")
    Long getFullSizeByFileId(String fileId);

    void updateUrl(String prefix);

    @Select("SELECT * FROM files WHERE webdav_path = #{path}")
    FileInfo getFileByWebdavPath(String path);

    @Select("SELECT * FROM files WHERE webdav_path LIKE #{path} || '%' ORDER BY id DESC")
    List<FileInfo> getFilesByPathPrefix(String path);

    @Select("DELETE FROM files WHERE file_id = #{fileId}")
    void deleteFile(String fileId);
}
