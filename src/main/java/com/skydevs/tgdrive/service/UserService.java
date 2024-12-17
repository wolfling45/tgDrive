package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.dto.AuthRequest;
import com.skydevs.tgdrive.entity.User;

public interface UserService {

    /**
     * 用户登入
     * @param authRequest 请求参数（用户名，密码）
     * @return User
     */
    User login(AuthRequest authRequest);
}
