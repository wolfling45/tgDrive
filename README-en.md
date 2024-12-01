# tgDrive - Unlimited Storage and Speed Cloud Drive

> [简体中文](./README.md)

**tgDrive** is a cloud storage application developed in Java, based on Telegram Bot, offering unlimited storage capacity and speed for file storage.

### Current Features:
- **Multithreaded Upload and Download**: Maximizes Telegram's speed limit for efficient file transfers.
- **Supports Image Direct Links**: Directly access download links to view images in the browser.
- **PicGo Support**: Fast image hosting integration with PicGo.

![tgDrive Upload Interface Demo](https://github.com/user-attachments/assets/5cbe3228-e425-4ece-84ac-6f1616f54be9)

[Render Deployment Site (Recommended for better speed)](https://tgdrive-latest.onrender.com/upload)

[Demo Site: tgDrive](https://server.skydevs.link/upload)

Frontend repository of this project: [tgDriveFront](https://github.com/SkyDependence/tgDrive-front)

[Self-deployment Guide](#self-deployment)  |  [PicGo Configuration](#picgo-configuration)  |  [Docker Deployment](#docker-deployment)  |  [Render Deployment](#render-deployment)

Currently, tgDrive is gradually being developed as a cloud drive, but it is already highly mature as an image hosting solution.

## Self-Deployment

### Environment Requirements
- Java 17+

### How to Use
1. Go to the [release page](https://github.com/SkyDependence/tgDrive/releases) to download the latest binary package.
2. Once downloaded, navigate to the directory where the binary package is stored.
3. Run the following command to start the binary package:
   ```
   java -jar tgDrive-0.0.2-SNAPSHOT.jar
   ```
4. Once the server is running, open your browser and enter `localhost:8085` to start using tgDrive.

Upon opening, you will see the following page:

![tgDrive Initial Page](https://github.com/user-attachments/assets/d82ff412-f75f-4179-b0d7-89dcf88d73cc)

## Configuration Parameters Explanation

- **Configuration File Name**: You can name it freely, primarily to identify which Bot and upload settings you are currently using.
- **botToken**: The API Token for your Telegram Bot. For instructions on how to create a Telegram Bot, refer to [this article](https://skydevs.link/posts/tech/telegram_bot).
- **chatID**: Usually your user ID. How to get chatID? Refer to [this article](https://skydevs.link/posts/tech/telegram_bot), check the last section.
- **url (optional)**: Currently has no function, filling it is optional.
- **Pass (optional)**: This feature is still under development.

After filling in the above configuration, click **Submit**. If you receive a "Submission Successful" prompt, it means the configuration has been saved. You can now load the configuration by using the name you just provided.

After loading the configuration, navigate to the upload page where you can paste, drag, or select files for upload.

Once the upload is complete, a download link will be provided, and the uploaded files will also be displayed in the file list in the top-left corner of the page.

## PicGo Configuration

> [!TIP]
> PicGo is supported from version v0.0.4 onwards.

This project can be used with [PicGo](https://github.com/Molunerfinn/PicGo) to enable fast image hosting uploads.

Before using, ensure the `web-uploader` plugin is installed.

![PicGo Configuration Page](https://github.com/user-attachments/assets/fe52f47e-b2ab-4751-bb65-7ead9ebce2c0)

### Parameter Explanation

- **API Address**: The default address for local deployment is `http://localhost:8085/api/upload`. If deployed on a server, change `http://localhost:8085` to your server's address, e.g., `http://233.233.233.233:8085/api/upload` or `http://example.com:8085/api/upload`.
- **POST Parameter Name**: Default is `file`.
- **JSON Path**: Default is `data.downloadLink`.

![PicGo Example](https://github.com/user-attachments/assets/dffeeb23-8f63-4bdb-a676-0bd693a2bede)

## Docker Deployment

Pull the Docker image:

```
docker pull nanyangzesi/tgdrive:latest
```

Run the container:

```
docker run -d -p 8085:8085 --name tgdrive nanyangzesi/tgdrive:latest
```

Run the container and set it to start automatically on boot:
```
docker run -d -p 8085:8085 --name tgdrive --restart always nanyangzesi/tgdrive:latest
```

## Render Deployment

> [!TIP]
> Even free deployments on Render require bank card verification.

Create a new Web Service.

![Create a Web Service](https://github.com/user-attachments/assets/543abbd1-0b2e-4892-8e46-265539159831)

Select "Exciting image" and input the Docker image of my project `nanyangzesi/tgdrive:latest`.

![Docker Image](https://github.com/user-attachments/assets/09f212c1-886b-424e-8015-a8f96f7e48ee)

Select the free instance.

![Free Instance](https://github.com/user-attachments/assets/18506bfa-9dda-4c41-a1eb-6cd7206c6f4b)

Scroll down to the bottom and click "Deploy web service". Wait for the deployment to complete, and your tgDrive instance will be successfully deployed! 🎉

## Support & Feedback

If you find this project helpful, please leave a star to support me! Your support is my biggest motivation!

