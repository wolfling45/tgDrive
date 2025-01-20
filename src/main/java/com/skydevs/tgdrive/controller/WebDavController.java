package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.FileService;
import com.skydevs.tgdrive.service.WebDacService;
import com.skydevs.tgdrive.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;

@RestController
@Slf4j
@RequestMapping("/webdav")
public class WebDavController {
    @Autowired
    private FileService fileService;
    @Autowired
    private WebDacService webDacService;

    /**
     * 上传文件
     * @param request
     * @return
     */
    @PutMapping("/**")
    public Result<Void> handlePut(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream()) {
            fileService.uploadByWebDav(inputStream, request);
            return Result.success();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    /**
     * 下载文件
     * @param
     * @return
     */
    @GetMapping("/**")
    public ResponseEntity<StreamingResponseBody> handleGet(HttpServletRequest request) {
        return fileService.downloadByWebDav(request.getRequestURI().substring("/webdav".length()));
    }

    /**
     * 删除文件
     * @param request
     * @param response
     * @return
     */
    @DeleteMapping("/**")
    public Result<Void> handleDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            fileService.deleteByWebDav(StringUtil.getPath(request.getRequestURI()));
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败");
        }
    }

    /**
     * 处理探测请求
     * @param response
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void handleOptions(HttpServletResponse response) {
        response.setHeader("Allow", "OPTIONS, HEAD, GET, POST, PROPFIND, MKCOL, MOVE, COPY");
        response.setHeader("DAV", "1,2");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * 处理特殊的webdav方法
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/dispatch/**", method = {RequestMethod.POST})
    public void handleWebDav(HttpServletRequest request, HttpServletResponse response) throws IOException {
        webDacService.switchMethod(request, response);
    }


}
