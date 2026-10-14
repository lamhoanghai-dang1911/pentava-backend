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

COMPOSE_CMD=""
if docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo "⚠️ Chưa có Docker Compose trên VPS, đang tự động cài đặt..."
    apt-get update -y && (apt-get install -y docker-compose-plugin || apt-get install -y docker-compose)
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    elif command -v docker-compose >/dev/null 2>&1; then
        COMPOSE_CMD="docker-compose"
    fi
fi

if [ -z "$COMPOSE_CMD" ]; then
    echo "❌ LỖI: Không tìm thấy lệnh docker compose hoặc docker-compose!"
    exit 1
fi

# Bật Docker BuildKit để build song song, tận dụng cache và giảm build context
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Tải trước base image (có retry 3 lần) để tránh lỗi TLS handshake timeout khi mạng VPS chập chờn
echo "📦 Tải trước base images (Maven & JRE)..."
docker pull maven:3.9-eclipse-temurin-21 || (sleep 3 && docker pull maven:3.9-eclipse-temurin-21) || true
docker pull eclipse-temurin:21-jre || (sleep 3 && docker pull eclipse-temurin:21-jre) || true

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
