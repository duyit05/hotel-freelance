#!/usr/bin/env bash
# Backup database + ảnh upload. Chạy trên VPS (cron tự chạy lúc 2h sáng, xem setup-server.sh):
#   bash /opt/hotel/backup.sh
# Kết quả nằm trong /root/hotel-backups, giữ 14 ngày gần nhất.
#
# Nên copy backup ra NGOÀI VPS: cài rclone, cấu hình 1 remote (vd Google Drive tên "gdrive"),
# rồi đặt RCLONE_REMOTE=gdrive:hotel-backups trong dòng cron.
set -euo pipefail

cd "$(dirname "$0")"
BACKUP_DIR="${BACKUP_DIR:-/root/hotel-backups}"
KEEP_DAYS="${KEEP_DAYS:-14}"
TS="$(date +%F_%H%M)"
mkdir -p "$BACKUP_DIR"

echo "[$(date)] Bắt đầu backup"

# Database (định dạng custom, khôi phục bằng pg_restore)
docker compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' \
  > "$BACKUP_DIR/db_$TS.dump"

# Ảnh upload
docker compose exec -T backend tar -czf - -C /app/uploads . \
  > "$BACKUP_DIR/uploads_$TS.tar.gz"

# Xóa backup cũ
find "$BACKUP_DIR" -type f -mtime +"$KEEP_DAYS" -delete

# Copy ra ngoài VPS (nếu có cấu hình)
if [ -n "${RCLONE_REMOTE:-}" ] && command -v rclone >/dev/null; then
  rclone copy "$BACKUP_DIR" "$RCLONE_REMOTE" --max-age 2d
  echo "Đã copy lên $RCLONE_REMOTE"
fi

ls -lh "$BACKUP_DIR" | tail -n 4
echo "[$(date)] Backup xong"
