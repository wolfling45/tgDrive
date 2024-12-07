# tgDrive - Unlimited Storage and Speed Cloud Disk

> [中文版](./README.md)

**tgDrive** is a Telegram Bot-based cloud storage application developed in Java, supporting unlimited file storage capacity and speed.

## Features

- **Multi-threaded Upload and Download**: Maximizes Telegram’s speed limit for efficient file transfers.
- **Image Hotlinking**: Provides direct download links for displaying images in a browser.
- **PicGo Support**: Seamless integration with PicGo for quick image uploads.
- **GIF Support**: Resolves Telegram’s conversion of GIFs to MP4. (Currently supports GIF files larger than 10MB.)

![tgDrive Upload Interface](https://github.com/user-attachments/assets/5cbe3228-e425-4ece-84ac-6f1616f54be9)

[Render Deployment (Recommended)](https://render.skydevs.link/upload) | [Demo Site](https://server.skydevs.link/upload)

Frontend repository: [tgDriveFront](https://github.com/SkyDependence/tgDrive-front)

[Docker Compose Deployment](#docker-compose-deployment) | [Docker Deployment](#docker-deployment) | [Self-hosting Guide](#self-hosting) | [Render Deployment](#render-deployment) | [PicGo Configuration](#picgo-configuration) | [Reverse Proxy](#reverse-proxy)

The image hosting functionality is stable, and the cloud disk features are under active development.

---

## Docker Compose Deployment

Docker Compose is the recommended way to deploy tgDrive quickly.

### Recommended Configuration

Create a `docker-compose.yml` file in the project’s root directory with the following content:

```yaml
version: '3.8'
services:
  tgdrive:
    image: nanyangzesi/tgdrive:latest
    container_name: tgdrive
    ports:
      - "8085:8085"
    volumes:
      - ./db:/app/db  # Mount the container’s /app/db directory to the host’s ./db directory
    restart: always
```

### Start the Service

Run the following command to start the service:

```bash
docker-compose up -d
```

### Update the Image

With volume mounting, you can update the image and restart the container without losing database data:

```bash
docker compose pull
docker compose up -d
```

---

## Docker Deployment

1. Pull the image:

   ```bash
   docker pull nanyangzesi/tgdrive:latest
   ```

2. Run the container:

   ```bash
   docker run -d -p 8085:8085 --name tgdrive nanyangzesi/tgdrive:latest
   ```

3. Enable auto-start on boot:

   ```bash
   docker run -d -p 8085:8085 --name tgdrive --restart always nanyangzesi/tgdrive:latest
   ```

### Migrate Existing Data

If you have previously run the project and generated database files within a container, you can migrate these files to a persistent directory on the host:

1. Identify the old container ID or name:

   ```bash
   docker ps -a
   ```

2. Copy the database files from the container to the host:

   ```bash
   docker cp <container_id_or_name>:/app/db ./db
   ```

   - Replace `<container_id_or_name>` with the actual container identifier.
   - Copy the contents of the container’s `/app/db` directory to the `db` folder in the host’s current directory.

3. Restart the project:

   Use the updated `docker-compose.yml` file to restart the project:

   ```bash
   docker compose up -d
   ```

4. Verify the data:

   After restarting, the project should read data from the host’s `./db` folder.

---

## Self-hosting

### Requirements

- Java 17+

### Usage

1. Download the latest binary package from the [release page](https://github.com/SkyDependence/tgDrive/releases).
2. Navigate to the directory containing the downloaded binary package.
3. Run the following command:

   ```bash
   java -jar [binary_package_name]
   ```

   Example:

   ```bash
   java -jar tgDrive-0.0.2-SNAPSHOT.jar
   ```

4. Access `localhost:8085` in your browser to start using tgDrive.

Example interface after successful deployment:

![tgDrive Initial Page](https://github.com/user-attachments/assets/d82ff412-f75f-4179-b0d7-89dcf88d73cc)

---

## Render Deployment

> [!TIP] Render’s free deployment requires credit card verification.

### Steps

1. Create a Web Service.

   ![Create Web Service](https://github.com/user-attachments/assets/543abbd1-0b2e-4892-8e46-265539159831)

2. Select Docker image and enter `nanyangzesi/tgdrive:latest`.

   ![Enter Docker Image](https://github.com/user-attachments/assets/09f212c1-886b-424e-8015-a8f96f7e48ee)

3. Choose the free instance option.

   ![Choose Free Instance](https://github.com/user-attachments/assets/18506bfa-9dda-4c41-a1eb-6cd7206c6f4b)

4. Scroll to the bottom of the page and click **Deploy Web Service** to complete the deployment.

Once deployed, your tgDrive instance is live! 🎉

---

## PicGo Configuration

> [!TIP] PicGo support is available starting from v0.0.4+.

Integrate with [PicGo](https://github.com/Molunerfinn/PicGo) for quick image uploads.

### Preparation

Ensure the `web-uploader` plugin is installed in PicGo.

![PicGo Configuration Page](https://github.com/user-attachments/assets/fe52f47e-b2ab-4751-bb65-7ead9ebce2c0)

### Parameter Settings

- **API URL**: Default is `http://localhost:8085/api/upload` for local deployments. Replace `localhost` with your server’s address for remote deployments.
- **POST Parameter Name**: Default is `file`.
- **JSON Path**: Default is `data.downloadLink`.

![PicGo Configuration Example](https://github.com/user-attachments/assets/dffeeb23-8f63-4bdb-a676-0bd693a2bede)

---

## Reverse Proxy

Ensure proper request header settings when using Caddy or NGINX as a reverse proxy. Below are example configurations:

### Caddy Configuration

```caddyfile
example.com {
    reverse_proxy /api* localhost:8080 {
        header_up X-Forwarded-Proto {scheme}
        header_up X-Forwarded-Port {server_port}
    }
}
```

- `{scheme}`: Automatically detects the request protocol (HTTP or HTTPS).
- `{server_port}`: Automatically detects the client connection port (e.g., 443).

### NGINX Configuration

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

## Support and Feedback

If you find this project helpful, please give it a star! Thank you! 😺

Your support is my greatest motivation!
