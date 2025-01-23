package com.skydevs.tgdrive.Interceptor;

import com.skydevs.tgdrive.entity.User;
import com.skydevs.tgdrive.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class WebDavAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 读取 Authorization 头部信息
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"WebDAV\"");
            return false;
        }

        // 解码Base64验证
        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        String[] values = credentials.split(":", 2);
        String username = values[0];
        String password = values[1];
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        User user = userMapper.getUserByUsername(username);
        if (user == null || !user.getRole().equals("admin") || !user.getPassword().equals(password)) {
            log.info("用户名或密码错误");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"WebDAV\"");
            return false;
        }

        return true;
    }


}
