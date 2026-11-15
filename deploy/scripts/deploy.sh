#!/usr/bin/env bash
# Chạy trên MÁY DEV (Git Bash trên Windows cũng được), từ thư mục gốc dự án:
#   bash deploy/scripts/deploy.sh root@<IP-VPS>
# Build image trên máy dev (VPS yếu nên không build trên đó), gửi lên VPS rồi khởi động lại.
# Dữ liệu (database, ảnh) nằm trong Docker volume trên VPS nên không bị mất khi deploy.
set -euo pipefail

SERVER="${1:?Cách dùng: bash deploy/scripts/deploy.sh root@<IP-VPS>}"
APP_DIR=/opt/hotel
cd "$(dirname "$0")"          # deploy/scripts
ROOT=../..                    # thư mục gốc dự án
PROD=../prod

echo "== Build image (linux/amd64) =="
docker build --platform linux/amd64 -t hotel-backend:latest "$ROOT/back-end"
docker build --platform linux/amd64 -t hotel-frontend:latest "$ROOT/front-end"

echo "== Gửi image lên $SERVER =="
docker save hotel-backend:latest hotel-frontend:latest | gzip | ssh "$SERVER" "gunzip | docker load"

echo "== Gửi file cấu hình =="
ssh "$SERVER" "mkdir -p $APP_DIR"
# Trên VPS tất cả nằm phẳng trong /opt/hotel: compose.yaml, Caddyfile, backup.sh, .env
scp "$PROD/compose.yaml" "$PROD/Caddyfile" backup.sh "$SERVER:$APP_DIR/"
ssh "$SERVER" "test -f $APP_DIR/.env || { echo 'Thiếu $APP_DIR/.env trên VPS (xem .env.example)'; exit 1; }"

echo "== Khởi động lại =="
ssh "$SERVER" "cd $APP_DIR && docker compose up -d --remove-orphans && docker image prune -f && docker compose ps"

echo "Deploy xong."
