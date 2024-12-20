# tgDrive - 无限容量和速度的网盘

<div align="center">

![GitHub release (latest by date)](https://img.shields.io/github/v/release/SkyDependence/tgDrive)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/SkyDependence/tgDrive/docker-publish.yml)
![Docker Image Size](https://img.shields.io/docker/image-size/nanyangzesi/tgdrive/latest)
![GitHub stars](https://img.shields.io/github/stars/SkyDependence/tgDrive)
![GitHub forks](https://img.shields.io/github/forks/SkyDependence/tgDrive)
![GitHub issues](https://img.shields.io/github/issues/SkyDependence/tgDrive)
![GitHub license](https://img.shields.io/github/license/SkyDependence/tgDrive)

</div>

**tgDrive** 是一款使用 Java 开发的基于 Telegram Bot 的网盘应用，支持不限容量和速度的文件存储。通过多线程技术和优化的传输策略，为用户提供高效、可靠的云存储解决方案。

## 目录

- [功能特点](#功能特点)
- [快速开始](#快速开始)
- [部署方式](#部署方式)
  - [Docker Compose 部署](#docker-compose-部署)
  - [Docker 部署](#docker-部署)
  - [自部署](#自部署)
  - [Render 部署](#render-部署)
- [使用说明](#使用说明)
- [进阶配置](#进阶配置)
  - [PicGo 配置](#picgo-配置)
  - [反向代理](#反向代理)
- [支持与反馈](#支持与反馈)

## 功能特点

### 核心优势

- 🚀 **突破限制**：完全突破 Telegram Bot API 的 20MB 文件大小限制
- 📈 **多线程传输**：采用多线程上传下载技术，最大化利用带宽资源
- 🔗 **外链支持**：支持图片外链功能，可直接在浏览器中访问和预览
- 🖼️ **图床集成**：完美支持 PicGo 图床工具，提供便捷的图片托管服务
- 🎯 **GIF 优化**：解决 Telegram 自动将 GIF 转换为 MP4 的问题

### 技术特性

- ⚡ **高性能**：基于 Java 17+ 开发，确保稳定性和性能
- 🐳 **容器化**：提供 Docker 支持，简化部署和维护流程
- 💾 **数据持久化**：支持数据持久化存储，确保数据安全
- 🔄 **API 支持**：提供完整的 RESTful API 接口

## 快速开始

### 在线体验

- [Render 部署站点（推荐）](https://render.skydevs.link)
- [演示站点](https://server.skydevs.link)

### 相关资源

- 前端代码：[tgDriveFront](https://github.com/SkyDependence/tgDrive-front)
- 最新版本：[Releases](https://github.com/SkyDependence/tgDrive/releases)

## 部署方式

### Docker Compose 部署

>[!TIP]
>📌 **注意**：如果服务器内存较小（RAM ≤ 512MB），建议使用 `nanyangzesi/tgdrive:server-latest` 镜像

1. 创建 `docker-compose.yml` 文件：

   ```yaml
   services:
   tgdrive:
      image: nanyangzesi/tgdrive:latest
      container_name: tgdrive
      ports:
         - "8085:8085"
      volumes:
         - ./db:/app/db
      restart: always
   ```

2. 启动服务：

```bash
docker-compose up -d
```

#### 更新镜像

使用数据卷挂载后，每次更新镜像时，只需拉取镜像并重新启动容器即可，数据库数据不会丢失：

```bash
docker compose pull
docker compose up -d
```

### Docker 部署

基础部署命令：

```bash
docker pull nanyangzesi/tgdrive:latest
docker run -d -p 8085:8085 --name tgdrive --restart always nanyangzesi/tgdrive:latest
```

#### 迁移之前的数据

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

### 自部署

前置要求：

- Java 17 或更高版本

部署步骤：

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

### Render 部署

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

## 使用说明



## 进阶配置

### PicGo 配置


> [!TIP]
> 从 v0.0.4+ 开始支持 PicGo。

本项目支持结合 [PicGo](https://github.com/Molunerfinn/PicGo) 快速上传图片。

#### 使用前准备

确保已安装 PicGo 插件 `web-uploader`。

![PicGo 配置页面](https://github.com/user-attachments/assets/fe52f47e-b2ab-4751-bb65-7ead9ebce2c0)

#### 参数说明

- **API 地址**：本地默认 `http://localhost:8085/api/upload`。服务器部署请修改为 `http://<服务器地址>:8085/api/upload`。
- **POST 参数名**：默认为 `file`。
- **JSON 路径**：默认为 `data.downloadLink`。

![PicGo 配置完成示例](https://github.com/user-attachments/assets/dffeeb23-8f63-4bdb-a676-0bd693a2bede)

### 反向代理

#### Caddy 配置

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

#### NGINX 配置

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

## 支持与反馈

如果您觉得本项目对您有帮助，欢迎：

- ⭐ 给项目点个 Star
- 🔄 分享给更多的朋友
- 🐛 提交 Issue 或 Pull Request

您的支持是项目持续发展的动力！
