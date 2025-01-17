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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.apache.catalina.manager.JspHelper.escapeXml;

@RestController
@Slf4j
@RequestMapping("/webdav")
public class WebDavController {
    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     * @param request
     * @return
     */
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

    /**
     * 下载文件
     * @param
     * @return
     */
    @GetMapping("/**")
    public ResponseEntity<StreamingResponseBody> handleGet(HttpServletRequest request) {
        return fileService.downloadFromTelegram(request.getRequestURI().substring("/webdav".length()));
    }

    /**
     * 删除文件
     * @param path
     * @return
     */
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
        String realMethod = (String) request.getAttribute("X-HTTP-Method-Override");
        log.info("进入handleWebDav方法，真实的method是{}", realMethod);
        String realURI = request.getRequestURI().substring("/webdav/dispatch".length());
        log.info("请求路径是{}", realURI);

        if (realMethod == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-HTTP-Method-Override");
            return;
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

    /**
     * 处理目录探测
     * @param request
     * @param response
     * @throws IOException
     */
    private static final DateTimeFormatter RFC1123_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    private void handlePropFind(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(207); // 207 Multi-Status
        response.setContentType("application/xml;charset=UTF-8");

        String path = request.getRequestURI().substring("/webdav/dispatch".length());
        if (path.isEmpty()) {
            path = "/";
        }

        // 假设 fileService.listFiles(path) 返回一个 Map，其中 "files" 是 List<Map<String,Object>>
        Map<String, Object> files = fileService.listFiles(path);

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<D:multistatus xmlns:D=\"DAV:\">\n");

        // 添加当前目录(如果AList会去读取根目录的属性，建议用collection)
        xmlBuilder.append("<D:response>\n")
                .append("<D:href>").append(escapeXml(path)).append("</D:href>\n")
                .append("<D:propstat>\n")
                .append("<D:prop>\n")
                .append("<D:displayname>").append(escapeXml(getDisplayName(path))).append("</D:displayname>\n")
                .append("<D:getlastmodified>").append(RFC1123_FORMATTER.format(Instant.now())).append("</D:getlastmodified>\n")
                .append("<D:resourcetype><D:collection/></D:resourcetype>\n")  // 这里表示本节点是目录
                .append("</D:prop>\n")
                .append("<D:status>HTTP/1.1 200 OK</D:status>\n")
                .append("</D:propstat>\n")
                .append("</D:response>\n");

        // 遍历子项
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fileList = (List<Map<String, Object>>) files.get("files");
        for (Map<String, Object> file : fileList) {
            String fileName = (String) file.get("name");
            boolean isDir = (boolean) file.get("isDir"); // 需要你在后台区分文件/文件夹
            long size = (long) file.get("size");
            long modifiedTime = (long) file.get("modified"); // 单位: 秒或毫秒，请注意一致性

            // 构造子项路径
            String filePath = path.endsWith("/") ? path + fileName : path + "/" + fileName;
            Instant modifiedInstant = Instant.ofEpochSecond(modifiedTime); // 如果是毫秒级就 ofEpochMilli
            String lastModifiedStr = RFC1123_FORMATTER.format(modifiedInstant);

            xmlBuilder.append("<D:response>\n")
                    .append("<D:href>").append(escapeXml(filePath)).append("</D:href>\n")
                    .append("<D:propstat>\n")
                    .append("<D:prop>\n")
                    .append("<D:displayname>").append(escapeXml(fileName)).append("</D:displayname>\n")
                    .append("<D:getlastmodified>").append(lastModifiedStr).append("</D:getlastmodified>\n");

            if (isDir) {
                // 文件夹
                xmlBuilder.append("<D:resourcetype><D:collection/></D:resourcetype>\n");
            } else {
                // 普通文件
                xmlBuilder.append("<D:resourcetype/>\n");
                xmlBuilder.append("<D:getcontentlength>").append(size).append("</D:getcontentlength>\n");
                // 可选: xmlBuilder.append("<D:getcontenttype>image/png</D:getcontenttype>\n");
            }

            xmlBuilder.append("</D:prop>\n")
                    .append("<D:status>HTTP/1.1 200 OK</D:status>\n")
                    .append("</D:propstat>\n")
                    .append("</D:response>\n");
        }

        xmlBuilder.append("</D:multistatus>");
        response.getWriter().write(xmlBuilder.toString());
    }

    private String getDisplayName(String path) {
        return path.substring(path.lastIndexOf('/'));
    }
}
