#!/usr/bin/env bash
# Chuẩn bị VPS Ubuntu mới (chạy MỘT lần, bằng root):  bash setup-server.sh
# - Cài Docker
# - Tạo 2 GB swap
# - Mở tường lửa cho SSH, HTTP, HTTPS
# - Tạo thư mục /opt/hotel và lịch backup hằng ngày lúc 2h sáng
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Hãy chạy bằng root (sudo bash setup-server.sh)"; exit 1
fi

echo "== Cập nhật hệ thống =="
apt-get update -y && apt-get upgrade -y

echo "== Cài Docker =="
if ! command -v docker >/dev/null; then
  curl -fsSL https://get.docker.com | sh
fi
systemctl enable --now docker

echo "== Tạo swap 2 GB =="
if ! swapon --show | grep -q /swapfile; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
  # Chỉ dùng swap khi RAM gần hết
  sysctl vm.swappiness=10
  echo 'vm.swappiness=10' > /etc/sysctl.d/99-swappiness.conf
fi

echo "== Tường lửa =="
apt-get install -y ufw
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

echo "== Thư mục ứng dụng và lịch backup =="
mkdir -p /opt/hotel /root/hotel-backups
CRON_LINE='0 2 * * * bash /opt/hotel/backup.sh >> /var/log/hotel-backup.log 2>&1'
( crontab -l 2>/dev/null | grep -v 'hotel/backup.sh' ; echo "$CRON_LINE" ) | crontab -

echo
echo "Xong. Bước tiếp theo:"
echo "  1. Tạo file /opt/hotel/.env (xem deploy/prod/.env.example)"
echo "  2. Từ máy dev chạy: bash deploy/scripts/deploy.sh root@<IP-VPS>"
