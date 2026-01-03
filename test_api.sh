#!/bin/bash

# 等待服务启动
echo "等待服务启动..."
sleep 5

# 测试API端点
echo "测试 /api/dashboard/stats 端点..."
curl -v http://127.0.0.1:8080/api/dashboard/stats

echo ""
echo "测试 /api/dashboard/recent-files 端点..."
curl -v http://127.0.0.1:8080/api/dashboard/recent-files