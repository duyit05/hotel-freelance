package com.example.back_end.upload;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Nơi lưu file ảnh thực tế (MinIO hoặc thư mục local). Tên file đã được FileStorageService kiểm tra.
 * Ảnh bị xóa được chuyển vào thùng rác (không phục vụ ra ngoài), StorageCleanupJob xóa hẳn sau vài ngày.
 */
public interface StorageBackend {

    /** size = -1 nếu không biết trước kích thước */
    void put(String name, InputStream in, long size, String contentType);

    boolean exists(String name);

    Optional<StoredFile> get(String name);

    /** Ảnh đang dùng được (không tính thùng rác) */
    List<FileInfo> list();

    /** Chuyển file vào thùng rác, thời điểm vào thùng rác là lastModified của file trong đó. Không có thì bỏ qua */
    void moveToTrash(String name);

    List<FileInfo> listTrash();

    /** Xóa hẳn file trong thùng rác */
    void deleteFromTrash(String name);

    /** Nội dung file; người gọi phải đóng content sau khi dùng */
    record StoredFile(InputStream content, String contentType, long size) {
    }

    record FileInfo(String name, Instant lastModified) {
    }
}
