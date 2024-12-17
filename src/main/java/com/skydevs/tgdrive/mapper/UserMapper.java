package com.skydevs.tgdrive.mapper;

import com.skydevs.tgdrive.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users where username = #{username}")
    User getUserByUsername(String username);
}
