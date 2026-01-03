#!/bin/bash

# 切换到脚本目录
cd "$(dirname "$0")"

# 清理并准备Maven仓库
mkdir -p /tmp/maven-repo
export MAVEN_OPTS="-Dmaven.repo.local=/tmp/maven-repo"

# 使用Maven编译
echo "正在编译源代码..."
mvn -Dmaven.repo.local=/tmp/maven-repo clean compile

if [ $? -ne 0 ]; then
    echo "编译失败，请检查代码"
    exit 1
fi

# 启动应用
echo "启动会议管理系统..."
mvn -Dmaven.repo.local=/tmp/maven-repo spring-boot:run