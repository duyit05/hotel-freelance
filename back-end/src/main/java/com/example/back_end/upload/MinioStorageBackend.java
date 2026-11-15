package com.example.back_end.upload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.back_end.config.AppProperties;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;

/**
 * Lưu ảnh trên MinIO/S3 (app.storage=minio, tùy chọn; mặc định dùng LocalStorageBackend).
 * Khi khởi động: tạo bucket nếu chưa có, chép ảnh cũ trong app.upload-dir (nếu còn) lên MinIO.
 * Bucket để private, ảnh được trả về qua back-end tại /uploads/<tên-file>.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app", name = "storage", havingValue = "minio")
public class MinioStorageBackend implements StorageBackend {

    private static final long PART_SIZE = 10L * 1024 * 1024;

    private final MinioClient client;
    private final String bucket;

    public MinioStorageBackend(AppProperties props) {
        AppProperties.Minio cfg = props.minio();
        this.client = MinioClient.builder()
            .endpoint(cfg.endpoint())
            .credentials(cfg.accessKey(), cfg.secretKey())
            .build();
        this.bucket = cfg.bucket();
        ensureBucket(cfg.endpoint());
        migrateLocalFiles(Path.of(props.uploadDir()));
    }

    private void ensureBucket(String endpoint) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Đã tạo bucket MinIO '{}'", bucket);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Không kết nối được MinIO tại " + endpoint + " (bucket '" + bucket + "'). "
                    + "Hãy kiểm tra MinIO đang chạy hoặc đặt STORAGE_TYPE=local", e);
        }
    }

    /** Chép ảnh từ thư mục upload cũ (lưu local trước đây) lên MinIO, bỏ qua file đã có */
    private void migrateLocalFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            return;
        }
        int copied = 0;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String name = file.getFileName().toString();
                if (!FileStorageService.isValidName(name) || exists(name)) {
                    continue;
                }
                try (InputStream in = Files.newInputStream(file)) {
                    put(name, in, Files.size(file), FileStorageService.contentTypeOf(name));
                }
                copied++;
            }
        } catch (IOException e) {
            log.warn("Lỗi khi chép ảnh cũ từ {} lên MinIO", dir.toAbsolutePath(), e);
        }
        if (copied > 0) {
            log.info("Đã chép {} ảnh cũ từ {} lên MinIO", copied, dir.toAbsolutePath());
        }
    }

    @Override
    public void put(String name, InputStream in, long size, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(name)
                .stream(in, size, size < 0 ? PART_SIZE : -1)
                .contentType(contentType)
                .build());
        } catch (Exception e) {
            throw new IllegalStateException("Không lưu được ảnh lên MinIO: " + name, e);
        }
    }

    @Override
    public boolean exists(String name) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(name).build());
            return true;
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                return false;
            }
            throw new IllegalStateException("Lỗi MinIO khi kiểm tra ảnh: " + name, e);
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi MinIO khi kiểm tra ảnh: " + name, e);
        }
    }

    @Override
    public Optional<StoredFile> get(String name) {
        try {
            GetObjectResponse res = client.getObject(GetObjectArgs.builder().bucket(bucket).object(name).build());
            String type = res.headers().get("Content-Type");
            String length = res.headers().get("Content-Length");
            return Optional.of(new StoredFile(res,
                type != null ? type : FileStorageService.contentTypeOf(name),
                length != null ? Long.parseLong(length) : -1));
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                return Optional.empty();
            }
            throw new IllegalStateException("Lỗi MinIO khi đọc ảnh: " + name, e);
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi MinIO khi đọc ảnh: " + name, e);
        }
    }

    private static boolean isNotFound(ErrorResponseException e) {
        String code = e.errorResponse().code();
        return "NoSuchKey".equals(code) || "NoSuchObject".equals(code);
    }
}
