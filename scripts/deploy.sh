#!/bin/bash
set -e

# ==============================================================
# PENTAVA BACKEND - SCRIPT DEPLOY & DỌN DẸP TỰ ĐỘNG TRÊN VPS
# ==============================================================

# Xác định thư mục gốc của project (nơi chứa docker-compose.yml)
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "=========================================================="
echo " [1/5] Bắt đầu triển khai PENTAVA Backend"
echo " Thư mục: $PROJECT_DIR"
echo " Thời gian: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================================="

# 1. Kiểm tra file .env (tự động tạo từ .env.example nếu chưa có)
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        echo "⚠️ Không tìm thấy file .env, tự động sao chép từ .env.example..."
        cp .env.example .env
    else
        echo "❌ LỖI: File .env không tồn tại tại $PROJECT_DIR!"
        echo "👉 Vui lòng tạo file .env từ .env.example trước khi deploy."
        exit 1
    fi
fi

# 2. Cập nhật code mới nhất từ git branch main
echo "📥 [2/5] Đang pull mã nguồn mới nhất từ branch main..."
git fetch origin main
git reset --hard origin/main

# 3. Build & Khởi động lại các container
echo "🚀 [3/5] Tiến hành build và chạy container..."

# Đảm bảo có Docker Compose V2 hiện đại (loại bỏ hoàn toàn lỗi KeyError: 'ContainerConfig' của bản 1.29.2 cũ)
if ! docker compose version >/dev/null 2>&1 && ( ! command -v docker-compose >/dev/null 2>&1 || docker-compose version 2>&1 | grep -q "version 1\." ); then
    echo "⚠️ Đang nâng cấp Docker Compose lên V2 chính thức từ Docker..."
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -sSL https://github.com/docker/compose/releases/download/v2.30.0/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose
    ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/bin/docker-compose
fi

COMPOSE_CMD=""
if docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo "❌ LỖI: Không khởi tạo được docker compose V2!"
    exit 1
fi

# Chỉ bật BuildKit nếu VPS đã cài đặt plugin docker buildx, tránh lỗi thiếu component buildx
if docker buildx version >/dev/null 2>&1; then
    export DOCKER_BUILDKIT=1
    export COMPOSE_DOCKER_CLI_BUILD=1
else
    unset DOCKER_BUILDKIT
    unset COMPOSE_DOCKER_CLI_BUILD
fi

# Kiểm tra và tải trước base image (chỉ tải nếu máy chưa có) để tránh lỗi mạng Docker Hub
echo "📦 Kiểm tra base images (Maven & JRE)..."
if ! docker image inspect maven:3.9-eclipse-temurin-21 >/dev/null 2>&1; then
    echo "--> Chưa có maven:3.9-eclipse-temurin-21, đang tải..."
    docker pull maven:3.9-eclipse-temurin-21 || (sleep 3 && docker pull maven:3.9-eclipse-temurin-21) || true
else
    echo "--> Đã có sẵn maven:3.9-eclipse-temurin-21 trên VPS."
fi

if ! docker image inspect eclipse-temurin:21-jre >/dev/null 2>&1; then
    echo "--> Chưa có eclipse-temurin:21-jre, đang tải..."
    docker pull eclipse-temurin:21-jre || (sleep 3 && docker pull eclipse-temurin:21-jre) || true
else
    echo "--> Đã có sẵn eclipse-temurin:21-jre trên VPS."
fi

# Dọn dẹp các container bản V1 cũ (dùng tên pentava_ với dấu gạch dưới) tránh xung đột port
echo "🧹 Dọn dẹp container V1 cũ để giải phóng cổng mạng..."
docker rm -f $(docker ps -aq --filter "name=pentava_") 2>/dev/null || true

echo "--> Đang sử dụng lệnh: $COMPOSE_CMD"
$COMPOSE_CMD up -d --build --remove-orphans

# 4. Kiểm tra sức khỏe các dịch vụ
echo "🩺 [4/5] Đang kiểm tra trạng thái hoạt động của các container..."
sleep 15
$COMPOSE_CMD ps

# 5. Dọn dẹp tài nguyên thừa (Clean up)
echo "🧹 [5/5] Tiến hành làm sạch các image cũ & build cache thừa..."
# Xóa các Docker image dangling (<none>) tạo ra sau khi build image mới
docker image prune -f

# Dọn dẹp Docker build cache cũ để tránh làm đầy ổ cứng VPS
docker builder prune -f --filter "until=48h" || true

echo "=========================================================="
echo "✅ Triển khai và làm sạch hoàn tất thành công!"
echo "=========================================================="
