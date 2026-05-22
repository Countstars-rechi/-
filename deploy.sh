#!/bin/bash
#
# 电商系统 — 云端一键部署脚本
# 用法: ssh 到云服务器后执行 ./deploy.sh
#

set -e

echo "========================================="
echo "  电商系统部署脚本"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================="

# 1. 拉取最新代码
echo ""
echo "[1/4] 拉取最新代码..."
git pull origin main

# 2. 停止并移除旧容器
echo ""
echo "[2/4] 停止旧容器..."
docker-compose down

# 3. 重新构建镜像并后台启动
echo ""
echo "[3/4] 构建镜像并启动容器..."
docker-compose up -d --build

# 4. 清理无用的 Docker 镜像（释放磁盘空间）
echo ""
echo "[4/4] 清理无用镜像..."
docker image prune -f

# 显示状态
echo ""
echo "========================================="
echo "  部署完成！"
echo "========================================="
echo "  访问地址: http://$(curl -s ifconfig.me 2>/dev/null || echo 'YOUR_SERVER_IP'):8080/"
echo ""
echo "  查看日志: docker-compose logs -f app"
echo "  查看状态: docker-compose ps"
echo "========================================="
