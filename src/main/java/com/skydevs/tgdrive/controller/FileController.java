package com.skydevs.tgdrive.controller;

import com.alibaba.fastjson.JSON;
import com.skydevs.tgdrive.dto.Message;
import com.skydevs.tgdrive.dto.UploadFile;
import com.skydevs.tgdrive.result.PageResult;
import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.BotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api")
public class FileController {

    @Autowired
    private BotService botService;

    /**
     * 加载配置
     * @param filename
     * @return
     */
    @GetMapping("/config/{filename}")
    public Result<String> loadConfig(@PathVariable("filename") String filename) {
        if (botService.setBotToken(filename)) {
            log.info("加载配置成功");
            return Result.success("配置加载成功");
        } else {
            log.error("配置加载失败");
            return Result.error("配置加载失败");
        }
   }

    /**
     * 上传文件
     * @param multipartFiles
     * @return
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file")MultipartFile[] multipartFiles, HttpServletRequest request) {
        if (multipartFiles.length == 0 || multipartFiles == null) {
            return Result.error("上传的文件为空");
        }

        List<UploadFile> uploadFiles = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            UploadFile uploadFile = new UploadFile();
            if (!file.isEmpty()) {
                String scheme = request.getHeader("X-Forwarded-Proto"); // 从代理请求头中获取协议
                if (scheme == null) {
                    scheme = request.getScheme(); // 如果未设置请求头，则回退到 request.getScheme()
                }
                String protocol = scheme;// 获取协议 http 或 https
                String host = request.getServerName(); // 获取主机名 localhost 或实际域名
                int port = request.getServerPort(); // 获取端口号 8080 或其他
                String prefix = protocol + "://" + host + ":" + port;
                String downloadPath = botService.uploadFile(file, prefix);
                String downloadUrl;
                if (downloadPath == null) {
                    downloadUrl = "文件上传失败";
                } else {
                    downloadUrl = prefix + downloadPath;
                }
                uploadFile.setFileName(file.getOriginalFilename());
                uploadFile.setDownloadLink(downloadUrl);
                uploadFiles.add(uploadFile);
           } else {
                uploadFile.setFileName("文件不存在");
            }
        }

        String resultJSON = JSON.toJSONString(uploadFiles);
        return Result.success(resultJSON);
    }

    /**
     * 发送消息
     * @param message
     * @return
     */
    @PostMapping("/send-message")
    public Result<String> sendMessage(@RequestBody Message message){
        log.info("处理消息发送");
        if (botService.sendMessage(message.getMessage())) {
            return Result.success("消息发送成功: " + message);
        } else {
            return Result.error("消息发送失败");
        }
    }


    @GetMapping("/fileList")
    public Result<PageResult> getFileList(@RequestParam int page, @RequestParam int size)  {
        PageResult pageResult = botService.getFileList(page, size);
        return Result.success(pageResult);
    }
}