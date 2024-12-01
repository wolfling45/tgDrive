# tgDrive - 无限容量和速度的网盘

**tgDrive** 是一款使用 Java 开发的基于 Telegram Bot 的网盘应用，支持不限容量和速度的文件存储。

目前已完成：
- **多线程上传下载**：尽量达到 Telegram 的速率上限，实现高效的文件传输。
- **支持图片外链**：可直接访问下载链接，浏览器中展示图片。
- **PicGo 支持**：搭配 PicGo 使用，快速上传图床。

![tgDrive 上传界面展示](https://github.com/user-attachments/assets/5cbe3228-e425-4ece-84ac-6f1616f54be9)

[demo 站点：tgDrive](https://server.skydevs.link/upload)

本项目的前端地址：[tgDriveFront](https://github.com/SkyDependence/tgDrive-front)

[自部署指南](#自部署)  |  [PicGo 配置](#picgo-配置)  |  [Docker 部署]()

目前打算逐步开发为网盘，但作为图床的使用场景已经相当成熟。

## 自部署

### 环境要求
- Java 17+

### 使用方法
1. 前往 [release 页面](https://github.com/SkyDependence/tgDrive/releases) 下载最新的二进制包。
2. 下载完成后，进入存放二进制包的目录。
3. 使用以下命令运行二进制包：
   ```
   java -jar tgDrive-0.0.2-SNAPSHOT.jar
   ```
4. 运行成功后，在浏览器中输入 `localhost:8085` 以开始使用。

打开后，您将看到以下页面：

![tgDrive 初始页面](https://github.com/user-attachments/assets/d82ff412-f75f-4179-b0d7-89dcf88d73cc)

## 参数配置说明

- **配置文件名**：可以随意填写，主要用于标识您当前使用哪个 Bot 和文件上传配置。
- **botToken**：您的 Telegram Bot 的 API Token。如何创建 Telegram Bot？可以参考[这篇文章](https://skydevs.link/posts/tech/telegram_bot)。
- **chatID**：通常为您的用户 ID。如何获取 chatID？同样可以参考[这篇文章](https://skydevs.link/posts/tech/telegram_bot)，查找最后一栏即可。
- **url (选填)**：目前没有任何用途，无需填写，填入也无影响。
- **Pass (选填)**：功能正在开发中。

填写完上述配置后，点击 **提交**。若提示 "提交成功"，即表示配置已保存。此时可以加载配置，配置文件名为您刚刚填写的名字。

加载配置后进入上传页面，支持粘贴、拖拽或选择文件上传。

上传完成后会返回文件的下载路径，已上传的文件也会显示在页面左上角的文件列表中。

## PicGo 配置

> **Tips：** 在v0.0.4中支持PicGo

本项目可以与 [PicGo](https://github.com/Molunerfinn/PicGo) 一起使用，实现快速图床上传。

使用前，请确保已安装插件 `web-uploader`。

![PicGo 配置页面](https://github.com/user-attachments/assets/fe52f47e-b2ab-4751-bb65-7ead9ebce2c0)

### 参数说明

- **API 地址**：本地部署默认地址为 `http://localhost:8085/api/upload`。如果部署在服务器上，请将 `http://localhost:8085` 修改为您的服务器地址，例如：`http://233.233.233.233:8085/api/upload` 或 `http://example.com:8085/api/upload`。
- **POST 参数名**：默认为 `file`。
- **JSON 路径**：默认为 `data.downloadLink`。

![image](https://github.com/user-attachments/assets/dffeeb23-8f63-4bdb-a676-0bd693a2bede)

## Docker 部署

拉取镜像：

```
docker pull nanyangzesi/tgdrive:latest
```

运行容器：

```
docker run -d -p 8085:8085 --name tgdrive nanyangzesi/tgdrive:latest
```

运行容器且开机自启动：
```
docker run -d -p 8085:8085 --name tgdrive --restart always nanyangzesi/tgdrive:latest
```

## 支持与反馈

如果您觉得项目有帮助，请点个 Star 支持我谢谢喵！您的支持是我最大的动力！

