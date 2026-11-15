#!/usr/bin/env bash
# Backup database + ảnh upload. Chạy trên VPS (cron tự chạy lúc 2h sáng, xem setup-server.sh):
#   bash /opt/hotel/backup.sh
# Kết quả nằm trong /root/hotel-backups.
#
# Tiết kiệm ổ đĩa cho VPS nhỏ:
# - Database: mỗi ngày (file chỉ vài trăm KB – vài MB), giữ KEEP_DAYS ngày.
# - Ảnh: mỗi tuần một lần vào Chủ nhật (hoặc khi chưa có bản nào), chỉ giữ UPLOADS_KEEP bản mới nhất,
#   bỏ qua thùng rác .trash. Chạy ngay một bản ảnh: FORCE_UPLOADS=1 bash /opt/hotel/backup.sh
#
# Nên copy backup ra NGOÀI VPS: cài rclone, cấu hình 1 remote (vd Google Drive tên "gdrive"),
# rồi đặt RCLONE_REMOTE=gdrive:hotel-backups trong dòng cron.
set -euo pipefail

cd "$(dirname "$0")"
BACKUP_DIR="${BACKUP_DIR:-/root/hotel-backups}"
KEEP_DAYS="${KEEP_DAYS:-14}"
UPLOADS_KEEP="${UPLOADS_KEEP:-2}"
TS="$(date +%F_%H%M)"
mkdir -p "$BACKUP_DIR"

echo "[$(date)] Bắt đầu backup"

# Database (định dạng custom, đã nén, khôi phục bằng pg_restore)
docker compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' \
  > "$BACKUP_DIR/db_$TS.dump"
find "$BACKUP_DIR" -name 'db_*.dump' -type f -mtime +"$KEEP_DAYS" -delete

# Ảnh upload: Chủ nhật, khi chưa có bản nào, hoặc khi FORCE_UPLOADS=1
if [ "$(date +%u)" = 7 ] || [ "${FORCE_UPLOADS:-0}" = 1 ] \
   || ! ls "$BACKUP_DIR"/uploads_*.tar.gz >/dev/null 2>&1; then
  docker compose exec -T backend tar -czf - -C /app/uploads --exclude=./.trash . \
    > "$BACKUP_DIR/uploads_$TS.tar.gz"
  # Chỉ giữ UPLOADS_KEEP bản ảnh mới nhất
  ls -1t "$BACKUP_DIR"/uploads_*.tar.gz | tail -n +"$((UPLOADS_KEEP + 1))" | xargs -r rm -f
  echo "Đã backup ảnh"
fi

# Copy ra ngoài VPS (nếu có cấu hình)
if [ -n "${RCLONE_REMOTE:-}" ] && command -v rclone >/dev/null; then
  rclone copy "$BACKUP_DIR" "$RCLONE_REMOTE" --max-age 2d
  echo "Đã copy lên $RCLONE_REMOTE"
fi

du -sh "$BACKUP_DIR"
df -h / | tail -n 1
echo "[$(date)] Backup xong"
