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

echo "== Giới hạn log hệ thống (ổ 20 GB) =="
mkdir -p /etc/systemd/journald.conf.d
printf '[Journal]\nSystemMaxUse=200M\n' > /etc/systemd/journald.conf.d/size.conf
systemctl restart systemd-journald
# Log backup tự xoay vòng, giữ 4 tuần
cat > /etc/logrotate.d/hotel-backup <<'EOF'
/var/log/hotel-backup.log {
  weekly
  rotate 4
  compress
  missingok
  notifempty
}
EOF

echo "== Tự cài bản vá bảo mật =="
apt-get install -y unattended-upgrades
dpkg-reconfigure -f noninteractive unattended-upgrades

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
# Mỗi Chủ nhật 4h: xóa image cũ đã bị thay sau các lần deploy (không đụng image đang dùng và volume dữ liệu)
CRON_PRUNE='0 4 * * 0 docker image prune -f >> /var/log/hotel-backup.log 2>&1'
# VPS mới chưa có crontab thì "crontab -l" báo lỗi -> "|| true" để script không dừng giữa chừng
OTHER_JOBS=$(crontab -l 2>/dev/null | grep -v 'hotel/backup.sh' | grep -v 'docker image prune' || true)
printf '%s\n%s\n%s\n' "$OTHER_JOBS" "$CRON_LINE" "$CRON_PRUNE" | sed '/^$/d' | crontab -

echo
echo "Xong. Bước tiếp theo:"
echo "  1. Tạo file /opt/hotel/.env (xem deploy/prod/.env.example)"
echo "  2. Từ máy dev chạy: bash deploy/scripts/deploy.sh root@<IP-VPS>"
