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
echo "[1/5] 拉取最新代码..."
git pull origin main

# 2. Maven 编译（使用宿主机缓存，仅首次慢）
echo ""
echo "[2/5] Maven 编译..."
mvn clean package -DskipTests -q

# 3. 停止并移除旧容器
echo ""
echo "[3/5] 停止旧容器..."
docker-compose down

# 4. 重新构建镜像并后台启动
echo ""
echo "[4/5] 构建镜像并启动容器..."
docker-compose up -d --build

# 5. 清理无用镜像
echo ""
echo "[5/5] 清理无用镜像..."
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
