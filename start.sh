#!/bin/bash

# 检查Java版本
if ! java -version 2>&1 | grep -q "1.8"; then
    echo "需要Java 8或更高版本"
    exit 1
fi

# 清理并准备Maven仓库
mkdir -p /tmp/maven-repo
export MAVEN_OPTS="-Dmaven.repo.local=/tmp/maven-repo"

# 构建项目
cd "$(dirname "$0")"
mvn clean package -DskipTests

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "构建失败"
    exit 1
fi

# 启动应用
echo "启动会议管理系统..."
java -jar target/meeting-manager-1.0.0.jar