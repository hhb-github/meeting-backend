FROM maven:3.8.6-eclipse-temurin-8 AS builder
LABEL "language"="java"
LABEL "framework"="spring-boot"

WORKDIR /app

# 配置 Maven 使用阿里云镜像
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?><settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 直接复制所有文件并构建（不分离 pom.xml）
COPY . .
RUN mvn clean package -DskipTests -T 1C -q

FROM eclipse-temurin:8-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# 直接启动，不使用健康检查
CMD ["java", "-Xmx512m", "-Xms256m", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-jar", "app.jar"]