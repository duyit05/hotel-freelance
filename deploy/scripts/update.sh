#!/usr/bin/env bash
# Chạy TRÊN VPS (vd. qua Termius): lấy code mới nhất từ GitHub, build image ngay trên VPS rồi khởi động lại.
#   bash /opt/hotel-src/deploy/scripts/update.sh
# Code nằm ở /opt/hotel-src (git clone), chạy thật ở /opt/hotel (compose.yaml, Caddyfile, backup.sh, .env).
# Dữ liệu (database, ảnh) nằm trong Docker volume nên không bị mất khi cập nhật.
# Lần build đầu mất khoảng 5–10 phút trên VPS 1 vCPU, các lần sau nhanh hơn nhờ cache.
set -euo pipefail

SRC=/opt/hotel-src
APP_DIR=/opt/hotel

cd "$SRC"
echo "== Lấy code mới =="
git pull --ff-only

test -f "$APP_DIR/.env" || { echo "Thiếu $APP_DIR/.env (xem deploy/prod/.env.example)"; exit 1; }

# Build lần lượt từng image để không vượt RAM của VPS nhỏ
echo "== Build back-end =="
docker build -t hotel-backend:latest back-end
echo "== Build front-end =="
docker build -t hotel-frontend:latest front-end

echo "== Cập nhật cấu hình =="
mkdir -p "$APP_DIR"
cp deploy/prod/compose.yaml deploy/prod/Caddyfile deploy/scripts/backup.sh "$APP_DIR/"

echo "== Khởi động lại =="
cd "$APP_DIR"
docker compose up -d --remove-orphans
# Dọn image cũ và cache build quá 7 ngày cho đỡ tốn ổ
docker image prune -f
docker builder prune -f --filter until=168h
docker compose ps

echo "Cập nhật xong. Xem log back-end: cd $APP_DIR && docker compose logs -f backend"
