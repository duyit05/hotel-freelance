# Deploy

```
deploy/
├── dev/                    Dùng trên MÁY LẬP TRÌNH
│   └── compose.yaml        PostgreSQL cho dev (không bắt buộc nếu đã cài Postgres)
├── prod/                   Dùng trên VPS (được copy lên /opt/hotel)
│   ├── compose.yaml        3 container: frontend (Caddy), backend, postgres
│   ├── Caddyfile           Định tuyến + HTTPS tự động
│   └── .env.example        Mẫu biến môi trường (domain, mật khẩu)
└── scripts/
    ├── setup-server.sh     Chạy 1 lần trên VPS mới: Docker, swap, tường lửa, lịch backup
    ├── deploy.sh           Chạy trên máy dev: build image, đẩy lên VPS, khởi động lại
    └── backup.sh           Chạy trên VPS (cron hằng ngày): backup database + ảnh
```

## Dev: chạy PostgreSQL bằng Docker

```bash
cd deploy/dev
docker compose --env-file ../../back-end/.env up -d
```
Trong `back-end/.env` đặt `DB_URL=jdbc:postgresql://localhost:5433/hotel`.

## Prod: mô hình trên VPS

1 VPS Ubuntu (1 vCPU / 2 GB RAM là đủ) chạy 3 container:

```
Người dùng ──HTTPS──► frontend: Caddy (80/443) ── / → file Vue (dist)
                                       ├─ /api/*      → backend:8080
                                       └─ /uploads/*  → backend:8080
                      backend  (Spring Boot, ảnh lưu ở volume "uploads")
                      postgres (dữ liệu ở volume "pgdata", không mở cổng ra ngoài)
```

Trên VPS mọi file nằm phẳng trong `/opt/hotel`: `compose.yaml`, `Caddyfile`, `backup.sh`, `.env`.

Các lệnh dưới đây chạy bằng Git Bash **từ thư mục gốc dự án**.

## Lần đầu

**1. Trỏ domain**: tạo bản ghi `A` cho `example.com` và `www.example.com` trỏ về IP VPS.
Đợi DNS cập nhật (kiểm tra bằng `ping example.com`) trước khi chạy bước 4, nếu không Caddy không lấy được HTTPS.

**2. Chuẩn bị VPS**:
```bash
scp deploy/scripts/setup-server.sh root@<IP-VPS>:/root/
ssh root@<IP-VPS> "bash /root/setup-server.sh"
```

**3. Tạo file `.env` trên VPS**:
```bash
scp deploy/prod/.env.example root@<IP-VPS>:/opt/hotel/.env
ssh root@<IP-VPS>
nano /opt/hotel/.env        # sửa DOMAIN và TẤT CẢ mật khẩu
openssl rand -base64 48     # dùng lệnh này để tạo JWT_SECRET / mật khẩu
```

**4. Deploy** (máy dev cần bật Docker Desktop):
```bash
bash deploy/scripts/deploy.sh root@<IP-VPS>
```
Mở `https://example.com` để kiểm tra.

## Cập nhật phiên bản mới

Sửa code xong, chạy lại:
```bash
bash deploy/scripts/deploy.sh root@<IP-VPS>
```
Database và ảnh nằm trong Docker volume nên không bị mất.

## Lệnh hay dùng trên VPS

```bash
cd /opt/hotel
docker compose ps                    # trạng thái
docker compose logs -f backend       # xem log back-end
docker compose restart backend       # khởi động lại back-end
free -h                              # xem RAM
df -h                                # xem ổ cứng
```

## Backup và khôi phục

Backup tự chạy lúc 2h sáng, lưu ở `/root/hotel-backups`:
- Database: mỗi ngày, giữ 14 ngày (mỗi file chỉ vài MB).
- Ảnh: mỗi Chủ nhật, chỉ giữ 2 bản mới nhất (để không đầy ổ 20 GB).

Chạy tay: `bash /opt/hotel/backup.sh` (thêm `FORCE_UPLOADS=1` ở đầu để backup cả ảnh ngay).

**Quan trọng**: backup nằm trên chính VPS thì mất VPS là mất luôn. Nên cài
[rclone](https://rclone.org/install/), cấu hình một remote (vd Google Drive tên `gdrive`), rồi sửa cron
(`crontab -e`) thành:
```
0 2 * * * RCLONE_REMOTE=gdrive:hotel-backups bash /opt/hotel/backup.sh >> /var/log/hotel-backup.log 2>&1
```

Khôi phục:
```bash
cd /opt/hotel
# Database
docker compose exec -T postgres sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists' \
  < /root/hotel-backups/db_<ngày>.dump
# Ảnh
docker compose exec -T backend tar -xzf - -C /app/uploads < /root/hotel-backups/uploads_<ngày>.tar.gz
```

### Thùng rác ảnh

Ảnh bị bỏ khỏi phòng (hoặc phòng bị xóa), và ảnh upload mà không lưu vào phòng nào sau 1 ngày,
được chuyển vào `/app/uploads/.trash`. Back-end tự xóa hẳn sau 7 ngày (chạy lúc 3h30 sáng).
Lỡ xóa nhầm thì trong 7 ngày đó có thể lấy lại, rồi thêm lại link `/uploads/<tên-file>` cho phòng:
```bash
cd /opt/hotel
docker compose exec backend ls -lt /app/uploads/.trash        # xem ảnh trong thùng rác
docker compose exec backend mv /app/uploads/.trash/<tên-file> /app/uploads/
```
