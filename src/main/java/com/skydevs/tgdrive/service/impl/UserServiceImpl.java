package com.skydevs.tgdrive.service.impl;

import com.skydevs.tgdrive.dto.AuthRequest;
import com.skydevs.tgdrive.entity.User;
import com.skydevs.tgdrive.mapper.UserMapper;
import com.skydevs.tgdrive.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;

    /**
     * 根据用户名返回用户
     * @param username 用户名
     * @return User
     */
    private User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Override
    public User login(AuthRequest authRequest) {
        User user = getUserByUsername(authRequest.getUsername());

        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        String password = DigestUtils.md5DigestAsHex(authRequest.getPassword().getBytes());
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }
}
