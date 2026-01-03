# 🎯 会议管理系统后端

> 基于Spring Boot的会议文件处理与智能分析系统

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 项目概述

会议管理系统后端是一个基于Spring Boot构建的企业级会议文件处理平台，提供会议记录的存储、分析和智能生成功能。系统支持多种文件格式的上传、语音转文字、会议纪要的结构化生成等核心功能。

### ✨ 核心功能

#### 🗂️ 数据管理
- **会议记录管理**: 完整的会议信息存储和管理
- **参会人员管理**: 会议参与人员信息维护
- **会议主题分类**: 按主题对会议进行分类管理
- **会议决策跟踪**: 记录和跟踪会议决策事项
- **行动项管理**: 会议后续行动任务的分配和跟踪

#### 📊 智能分析
- **文件处理引擎**: 支持多种会议文件格式的处理
- **语音转文字**: 会议录音文件的语音识别转换
- **AI纪要生成**: 基于会议内容智能生成结构化纪要
- **数据统计分析**: 会议数据的统计分析和可视化

#### 🔧 系统功能
- **RESTful API**: 完整的REST API接口服务
- **文件上传**: 支持多文件格式的安全上传
- **数据验证**: 完善的数据校验和异常处理
- **分页查询**: 高效的数据分页和搜索功能

## 🚀 快速开始

### 环境要求

- **Java**: JDK 1.8 或更高版本
- **Maven**: 3.6 或更高版本
- **MySQL**: 8.0 或更高版本
- **Docker**: (可选) 用于容器化部署

### 本地开发

#### 1. 克隆项目
```bash
git clone https://github.com/hhb-github/meeting-backend.git
cd meeting-backend
```

#### 2. 数据库配置
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE meeting_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化脚本
mysql -u root -p meeting_manager < init.sql
```

#### 3. 配置应用
```bash
# 复制配置文件
cp src/main/resources/application.yml src/main/resources/application-local.yml

# 编辑数据库连接配置
# 修改 src/main/resources/application-local.yml 中的数据库连接信息
```

#### 4. 启动应用
```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

或使用提供的启动脚本：
```bash
chmod +x run_app.sh
./run_app.sh
```

应用将在 `http://localhost:8080/api` 启动

### Docker部署

#### 开发环境
```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

#### 生产环境
```bash
# 使用生产配置启动
docker-compose -f docker-compose.prod.yml up -d
```

## 🏗️ 项目结构

```
meeting-backend/
├── src/main/java/com/meeting/manager/
│   ├── config/           # 配置类
│   │   ├── HttpClientConfig.java
│   │   └── WebConfig.java
│   ├── controller/       # 控制器层
│   │   ├── DashboardController.java    # 仪表板统计
│   │   └── MeetingRecordController.java # 会议记录管理
│   ├── dto/              # 数据传输对象
│   │   ├── ChatRequest.java
│   │   ├── FileUploadRequest.java
│   │   └── StructuredMeetingSummary.java
│   ├── entity/           # 实体类
│   │   ├── MeetingActionItem.java      # 会议行动项
│   │   ├── MeetingDecision.java        # 会议决策
│   │   ├── MeetingFollowUp.java        # 会议跟进
│   │   ├── MeetingParticipant.java     # 参会人员
│   │   ├── MeetingRecord.java          # 会议记录
│   │   └── MeetingTopic.java           # 会议主题
│   ├── exception/        # 异常处理
│   │   └── GlobalExceptionHandler.java
│   ├── repository/       # 数据访问层
│   │   ├── MeetingActionItemRepository.java
│   │   ├── MeetingDecisionRepository.java
│   │   ├── MeetingFollowUpRepository.java
│   │   ├── MeetingParticipantRepository.java
│   │   ├── MeetingRecordRepository.java
│   │   └── MeetingTopicRepository.java
│   ├── service/          # 业务逻辑层
│   │   ├── FileProcessingService.java     # 文件处理服务
│   │   ├── FileStorageService.java        # 文件存储服务
│   │   ├── MeetingAnalysisService.java    # 会议分析服务
│   │   └── SpeechToTextService.java       # 语音转文字服务
│   └── MeetingManagerApplication.java     # 主启动类
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   ├── application-prod.yml         # 生产配置
│   └── application.properties       # 传统配置
├── init.sql                         # 数据库初始化脚本
├── Dockerfile                       # Docker镜像配置
├── docker-compose.yml               # Docker Compose配置
└── pom.xml                          # Maven项目配置
```

## 🛠️ 技术栈

### 核心框架
- **Spring Boot 2.7.18**: 微服务框架
- **Spring Data JPA**: 数据访问抽象
- **Spring Validation**: 数据验证框架
- **Spring Web**: RESTful Web服务

### 数据存储
- **MySQL 8.0**: 主数据库
- **Hibernate JPA**: ORM框架
- **Maven**: 项目构建工具

### 开发工具
- **Lombok**: 减少样板代码
- **Jackson**: JSON处理
- **SLF4J + Logback**: 日志框架

## 📚 API文档

### 基础信息
- **基础URL**: `http://localhost:8080/api`
- **认证方式**: 当前版本无需认证（生产环境请添加认证）
- **数据格式**: JSON

### 主要接口

#### 仪表板统计
```
GET /dashboard/stats
```
获取系统统计数据

**请求参数**:
- `startDate` (可选): 开始日期 (格式: yyyy-MM-dd)
- `endDate` (可选): 结束日期 (格式: yyyy-MM-dd)

**响应示例**:
```json
{
  "totalMeetings": 150,
  "totalParticipants": 320,
  "recentMeetings": [...],
  "meetingStats": [...]
}
```

#### 会议记录管理
```
GET /meeting-records
```
获取会议记录列表（支持分页和搜索）

**请求参数**:
- `page` (可选): 页码 (默认: 0)
- `size` (可选): 每页大小 (默认: 10)
- `search` (可选): 搜索关键词
- `startDate` (可选): 开始日期
- `endDate` (可选): 结束日期

```
POST /meeting-records
```
创建新的会议记录

**请求参数**:
- `meetingName`: 会议名称
- `meetingTopic`: 会议主题
- `meetingDate`: 会议日期
- `file`: 会议文件

```
GET /meeting-records/{id}
```
获取指定会议记录详情

```
PUT /meeting-records/{id}
```
更新会议记录

```
DELETE /meeting-records/{id}
```
删除会议记录

#### 参会人员管理
```
GET /meeting-records/{meetingId}/participants
```
获取指定会议的参会人员列表

```
POST /meeting-records/{meetingId}/participants
```
添加参会人员

```
DELETE /meeting-records/{meetingId}/participants/{participantId}
```
移除参会人员

### 健康检查
```
GET /health
```
系统健康状态检查

**响应示例**:
```json
{
  "status": "UP",
  "timestamp": "2024-01-01T10:00:00",
  "database": "UP"
}
```

## 🔧 配置说明

### 应用配置
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: meeting-manager
  
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_manager
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
```

### 环境变量
- `DB_HOST`: 数据库主机地址 (默认: localhost)
- `DB_PORT`: 数据库端口 (默认: 3306)
- `DB_NAME`: 数据库名称 (默认: meeting_manager)
- `DB_USERNAME`: 数据库用户名 (默认: root)
- `DB_PASSWORD`: 数据库密码 (默认: 123456)
- `SERVER_PORT`: 应用端口 (默认: 8080)

## 🧪 测试

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=MeetingRecordControllerTest

# 生成测试覆盖率报告
mvn jacoco:report
```

### API测试
使用提供的测试脚本：
```bash
# 测试API接口
./test_api.sh
```

## 📦 部署

### 生产部署

#### 1. 使用Docker
```bash
# 构建生产镜像
docker build -t meeting-backend:prod .

# 运行容器
docker run -d \
  --name meeting-backend \
  -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_PASSWORD=your-secure-password \
  meeting-backend:prod
```

#### 2. 使用JAR包
```bash
# 编译生产版本
mvn clean package -Pprod

# 运行应用
java -jar target/meeting-manager-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### 监控和日志
- **健康检查**: `GET /health`
- **指标监控**: Spring Boot Actuator (如已启用)
- **日志位置**: `logs/meeting-manager.log`

## 🤝 贡献指南

### 开发流程
1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范
- 遵循Spring Boot官方编码规范
- 添加适当的单元测试
- 确保代码覆盖率 > 80%
- 提交前运行 `mvn clean compile`

### 提交信息格式
```
feat: 添加会议文件批量上传功能
fix: 修复参会人员查询分页问题
docs: 更新API文档
refactor: 重构文件处理服务代码
test: 添加会议记录控制器测试用例
```

## 📝 更新日志

### v1.0.0 (2024-01-01)
- ✨ 初始版本发布
- ✨ 会议记录CRUD功能
- ✨ 参会人员管理
- ✨ 文件上传和处理
- ✨ 仪表板统计功能
- ✨ Docker容器化支持

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 团队

- **后端开发**: [hhb-github](https://github.com/hhb-github)
- **项目地址**: [meeting-backend](https://github.com/hhb-github/meeting-backend)

## 📞 联系我们

如有问题或建议，请通过以下方式联系：

- 🐛 **Bug报告**: [GitHub Issues](https://github.com/hhb-github/meeting-backend/issues)
- 💡 **功能请求**: [GitHub Discussions](https://github.com/hhb-github/meeting-backend/discussions)
- 📧 **邮件**: [your-email@example.com](mailto:your-email@example.com)

---

<div align="center">
  <strong>🎯 让会议管理更智能，让团队协作更高效！</strong>
</div>