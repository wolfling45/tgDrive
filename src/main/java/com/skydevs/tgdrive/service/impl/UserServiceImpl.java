package com.skydevs.tgdrive.service.impl;

import com.skydevs.tgdrive.dto.AuthRequest;
import com.skydevs.tgdrive.entity.User;
import com.skydevs.tgdrive.exception.PasswordErrorException;
import com.skydevs.tgdrive.exception.UserNotFoundException;
import com.skydevs.tgdrive.mapper.UserMapper;
import com.skydevs.tgdrive.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名返回用户
     * @param username 用户名
     * @return User
     */
    private User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    /**
     * 用户登入
     * @param authRequest 请求参数（用户名，密码）
     * @return
     */
    @Override
    public User login(AuthRequest authRequest) {
        User user = getUserByUsername(authRequest.getUsername());

        if (user == null) {
            throw new UserNotFoundException();
        }

        String password = DigestUtils.md5DigestAsHex(authRequest.getPassword().getBytes());
        if (!password.equals(user.getPassword())) {
            throw new PasswordErrorException();
        }

        return user;
    }
}
