# 一款使用Java开发的，基于telegram bot的不限制容量，速度的网盘

使用多线程上传和下载，尽量达到telegram的速率限制

图片做了外链，直接访问下载链接就会在浏览器展示图片，展示如下：

![image](https://github.com/user-attachments/assets/5cbe3228-e425-4ece-84ac-6f1616f54be9)

准备开发成网盘，目前还是作为图床用到的比较多

## 环境
java17+

## 使用方法
在release页面下载最新的二进制包

下载好后，进入你存放二进制包的目录

使用java -jar [二进制包] 来运行，例如：

```
java -jar tgDrive-0.0.2-SNAPSHOT.jar
```

运行成功后在浏览器输入localhost:8080来开始使用

打开后，你会看到以下页面：

![image](https://github.com/user-attachments/assets/d82ff412-f75f-4179-b0d7-89dcf88d73cc)

### 参数说明
- 配置文件名：取一个你顺手的名字即可，是用来确定你是使用哪个bot、对象发送文件的
- botToken：你的telegram bot 的API token，如何创建一个telegram bot？可以参考[这篇文章](https://skydevs.link/posts/tech/telegram_bot)
- chatID：一般来说，就是你的用户ID，如何获取？可以参考[这篇文章](https://skydevs.link/posts/tech/telegram_bot)，最后一栏就是
- url (选填)：目前没有任何用处，不需要填，当然你填了也没什么
- Pass (选填)：等待施工

填写好了上述配置后，点击提交，看到“提交成功”提示后，就可以加载配置，配置文件名就是你刚刚填写的配置文件名

加载配置后就来到了上传页面，支持粘贴、拖拽、选择文件

选择文件上传后等待上传即可，上传完成后会返回文件的下载路径，已上传的文件也会保存在左上角的文件列表中

点个star谢谢喵！你的支持是我最大的动力！
