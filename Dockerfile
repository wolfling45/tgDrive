# 设置基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录为 /app
WORKDIR /app

# 将 JAR 文件复制到工作目录
COPY target/tgDrive-0.0.6-SNAPSHOT.jar app.jar

# 容器启动时运行的命令
CMD ["java", "-jar", "app.jar"]
