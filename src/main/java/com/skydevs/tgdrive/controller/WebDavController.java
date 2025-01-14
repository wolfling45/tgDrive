package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.FileService;
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

    @PutMapping("/**")
    public Result<Void> handlePut(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream()) {
            fileService.uploadToTelegram(inputStream, request);
            return Result.success();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    @GetMapping("/{path:.+}")
    public ResponseEntity<StreamingResponseBody> handleGet(@PathVariable String path) {
        return fileService.downloadFromTelegram(path)
                .map(stream -> ResponseEntity.ok()
                        .header("Content-Disposition", "attachment")
                        .body(stream))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{path:.+}")
    public Result<Void> handleDelete(@PathVariable String path) {
        try {
            fileService.deleteFromTelegram(path);
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败");
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void handleOptions(HttpServletResponse response) {
        response.setHeader("Allow", "OPTIONS, HEAD, GET, POST, PROPFIND, MKCOL, MOVE, COPY");
        response.setHeader("DAV", "1,2");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/dispatch/**", method = {RequestMethod.POST})
    public void handleWebDav(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String realMethod = (String) request.getAttribute("X-HTTP-Method-Override");
        log.info("进入handleWebDav方法，真实的method是{}", realMethod);
        String realURI = request.getRequestURI().substring("/webdav/dispatch".length());
        log.info("请求路径是{}", realURI);

        if (realMethod == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-HTTP-Method-Override");
        }
        switch (realMethod.toUpperCase()) {
            case "PROPFIND":
                handlePropFind(request, response);
                break;
            case "MKCOL":
                handleMkCol(request, response);
                break;
            case "MOVE":
                handleMove(request, response);
                break;
            case "COPY":
                handleCopy(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Unsupported WebDAV method");
                break;
        }
    }

    private void handleMove(HttpServletRequest request, HttpServletResponse response) {

    }

    private void handleMkCol(HttpServletRequest request, HttpServletResponse response) {

    }

    private void handleCopy(HttpServletRequest request, HttpServletResponse response) {

    }

    private void handlePropFind(HttpServletRequest request, HttpServletResponse response) throws IOException{
        log.info("处理 PROPFIND...");
        // 返回一个示例 Multi-Status
        response.setStatus(207); // 207 Multi-Status
        response.setContentType("application/xml;charset=UTF-8");
        String body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <multistatus xmlns="DAV:">
                    <!-- 根目录 -->
                    <response>
                        <href>/webdav/</href>
                        <propstat>
                            <prop>
                                <displayname>webdav</displayname>
                                <getlastmodified>Wed, 01 Jan 2023 00:00:00 GMT</getlastmodified>
                                <resourcetype>
                                    <collection/>
                                </resourcetype>
                            </prop>
                            <status>HTTP/1.1 200 OK</status>
                        </propstat>
                    </response>
                                
                    <!-- 文件夹 docs -->
                    <response>
                        <href>/webdav/docs/</href>
                        <propstat>
                            <prop>
                                <displayname>docs</displayname>
                                <getlastmodified>Wed, 01 Jan 2023 00:00:00 GMT</getlastmodified>
                                <resourcetype>
                                    <collection/>
                                </resourcetype>
                            </prop>
                            <status>HTTP/1.1 200 OK</status>
                        </propstat>
                    </response>
                                
                    <!-- 文件 example.txt -->
                    <response>
                        <href>/webdav/example.txt</href>
                        <propstat>
                            <prop>
                                <displayname>example.txt</displayname>
                                <getlastmodified>Wed, 01 Jan 2023 00:00:00 GMT</getlastmodified>
                                <getcontentlength>1024</getcontentlength> <!-- 文件大小 -->
                                <resourcetype/> <!-- 文件没有 <collection/> 标签 -->
                            </prop>
                            <status>HTTP/1.1 200 OK</status>
                        </propstat>
                    </response>
                </multistatus>

                """;
        response.getWriter().write(body);

    }
}
