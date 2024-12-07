# tgDrive - 无限容量和速度的网盘

> [English Version](./README-en.md)

**tgDrive** 是一款使用 Java 开发的基于 Telegram Bot 的网盘应用，支持不限容量和速度的文件存储。

## 功能特点

- **多线程上传下载**：尽可能达到 Telegram 的速率上限，实现高效文件传输。
- **支持图片外链**：可直接访问下载链接，在浏览器中展示图片。
- **PicGo 支持**：结合 PicGo 使用，实现快速图床上传。
- **GIF 支持**：解决 Telegram 将 GIF 转为 MP4 的问题。（目前仅支持大于10MB的GIF文件）

![tgDrive 上传界面展示](https://github.com/user-attachments/assets/5cbe3228-e425-4ece-84ac-6f1616f54be9)

[Render 部署的站点（推荐）](https://render.skydevs.link/upload)  |  [demo 站点](https://server.skydevs.link/upload)

前端代码地址：[tgDriveFront](https://github.com/SkyDependence/tgDrive-front)

[Docker Compose 部署](#docker-compose-部署)  |  [Docker 部署](#docker-部署)  |  [自部署指南](#自部署)  |  [Render 部署](#render-部署)  |  [PicGo 配置](#picgo-配置)  |  [反向代理](#反向代理)

目前已作为图床功能成熟，网盘功能正在逐步开发。

---

## Docker Compose 部署

推荐通过 Docker Compose 快速部署 tgDrive。

### 推荐配置

在项目根目录创建 `docker-compose.yml` 文件，内容如下：

```yaml
version: '3.8'
services:
  tgdrive:
    image: nanyangzesi/tgdrive:latest
    container_name: tgdrive
    ports:
      - "8085:8085"
    volumes:
      - ./db:/app/db  # 将容器内的 /app/db 目录挂载到主机的 ./db 目录
    restart: always
```

### 启动服务

运行以下命令启动服务：

```bash
docker-compose up -d
```

### 更新镜像

使用数据卷挂载后，每次更新镜像时，只需拉取镜像并重新启动容器即可，数据库数据不会丢失：

```bash
docker compose pull
docker compose up -d
```

---

## Docker 部署

1. 拉取镜像：

   ```bash
   docker pull nanyangzesi/tgdrive:latest
   ```

2. 运行容器：

   ```bash
   docker run -d -p 8085:8085 --name tgdrive nanyangzesi/tgdrive:latest
   ```

3. 开机自启动：

   ```bash
   docker run -d -p 8085:8085 --name tgdrive --restart always nanyangzesi/tgdrive:latest
   ```

### 迁移之前的数据

如果您已经运行过项目，并在容器内生成了数据库文件，可以将这些数据手动迁移到主机的持久化目录中：

1. 找到旧容器的 ID 或名称：

   ```bash
   docker ps -a
   ```

2. 复制容器内的数据库文件到主机：

   ```bash
   docker cp <容器名或ID>:/app/db ./db
   ```

   - 将 `<容器名或ID>` 替换为实际的容器标识。
   - 将容器内的 `/app/db` 文件夹内容复制到主机的当前目录下的 `db` 文件夹。

3. 重新启动项目：

   使用更新后的 `docker-compose.yml`，重新启动项目：

   ```bash
   docker compose up -d
   ```

4. 验证数据：

   启动后，项目应能够读取到主机 `./db` 文件夹中的数据。

---

## 自部署

### 环境要求

- Java 17+

### 使用方法

1. 前往 [release 页面](https://github.com/SkyDependence/tgDrive/releases) 下载最新的二进制包。
2. 进入下载的二进制包所在目录。
3. 运行以下命令：

   ```bash
   java -jar [最新的二进制包名]
   ```

   例如：

   ```bash
   java -jar tgDrive-0.0.2-SNAPSHOT.jar
   ```

4. 运行成功后，在浏览器中访问 `localhost:8085` 开始使用。

运行后页面示例：

![tgDrive 初始页面](https://github.com/user-attachments/assets/d82ff412-f75f-4179-b0d7-89dcf88d73cc)

---

## Render 部署

> [!TIP]
> Render 免费部署需要银行卡认证。

### 步骤

1. 创建一个 Web Service。

   ![创建 Web Service](https://github.com/user-attachments/assets/543abbd1-0b2e-4892-8e46-265539159831)

2. 选择 Docker 镜像，填入 `nanyangzesi/tgdrive:latest`。

   ![镜像填写](https://github.com/user-attachments/assets/09f212c1-886b-424e-8015-a8f96f7e48ee)

3. 选择免费实例。

   ![选择免费实例](https://github.com/user-attachments/assets/18506bfa-9dda-4c41-a1eb-6cd7206c6f4b)

4. 滑动至页面底部，点击 **Deploy Web Service** 完成部署。

部署完成后，您的 tgDrive 实例已成功运行！🎉

---

## PicGo 配置

> [!TIP]
> 从 v0.0.4+ 开始支持 PicGo。

本项目支持结合 [PicGo](https://github.com/Molunerfinn/PicGo) 快速上传图片。

### 使用前准备

确保已安装 PicGo 插件 `web-uploader`。

![PicGo 配置页面](https://github.com/user-attachments/assets/fe52f47e-b2ab-4751-bb65-7ead9ebce2c0)

### 参数说明

- **API 地址**：本地默认 `http://localhost:8085/api/upload`。服务器部署请修改为 `http://<服务器地址>:8085/api/upload`。
- **POST 参数名**：默认为 `file`。
- **JSON 路径**：默认为 `data.downloadLink`。

![PicGo 配置完成示例](https://github.com/user-attachments/assets/dffeeb23-8f63-4bdb-a676-0bd693a2bede)

---

## 反向代理

确保在使用 Caddy 或 NGINX 反向代理时正确设置请求头。以下是示例配置：

### Caddy 配置

```caddyfile
example.com {
    reverse_proxy /api* localhost:8080 {
        header_up X-Forwarded-Proto {scheme}
        header_up X-Forwarded-Port {server_port}
    }
}
```

- `{scheme}`：根据实际请求的协议（HTTP 或 HTTPS）填充。
- `{server_port}`：自动获取客户端连接的端口（如 443）。

### NGINX 配置

```nginx
server {
    listen 443 ssl;
    server_name example.com;

    location / {
        proxy_pass http://localhost:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 支持与反馈

如果您觉得项目有帮助，请点个 Star 支持我，谢谢喵！

您的支持是我最大的动力！
