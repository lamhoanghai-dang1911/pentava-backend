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

# 1. Kiểm tra file .env
if [ ! -f .env ]; then
    echo "❌ LỖI: File .env không tồn tại tại $PROJECT_DIR!"
    echo "👉 Vui lòng tạo file .env từ .env.example trước khi deploy."
    exit 1
fi

# 2. Cập nhật code mới nhất từ git branch main
echo "📥 [2/5] Đang pull mã nguồn mới nhất từ branch main..."
git fetch origin main
git reset --hard origin/main

# 3. Build & Khởi động lại các container
echo "🚀 [3/5] Tiến hành build và chạy container (docker compose up -d --build)..."
docker compose up -d --build --remove-orphans

# 4. Kiểm tra sức khỏe các dịch vụ
echo "🩺 [4/5] Đang kiểm tra trạng thái hoạt động của các container..."
sleep 15
docker compose ps

# 5. Dọn dẹp tài nguyên thừa (Clean up)
echo "🧹 [5/5] Tiến hành làm sạch các image cũ & build cache thừa..."
# Xóa các Docker image dangling (<none>) tạo ra sau khi build image mới
docker image prune -f

# Dọn dẹp Docker build cache cũ để tránh làm đầy ổ cứng VPS
docker builder prune -f --filter "until=48h" || true

echo "=========================================================="
echo "✅ Triển khai và làm sạch hoàn tất thành công!"
echo "=========================================================="
