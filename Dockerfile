# 设置基础镜像
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录为 /app
WORKDIR /app

# 将 JAR 文件复制到工作目录
COPY target/tgDrive-0.0.9.jar app.jar

# 容器启动时运行的命令
CMD ["java", "-jar", "app.jar"]
