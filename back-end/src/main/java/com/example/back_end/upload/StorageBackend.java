package com.example.back_end.upload;

import java.io.InputStream;
import java.util.Optional;

/** Nơi lưu file ảnh thực tế (MinIO hoặc thư mục local). Tên file đã được FileStorageService kiểm tra. */
public interface StorageBackend {

    /** size = -1 nếu không biết trước kích thước */
    void put(String name, InputStream in, long size, String contentType);

    boolean exists(String name);

    Optional<StoredFile> get(String name);

    /** Nội dung file; người gọi phải đóng content sau khi dùng */
    record StoredFile(InputStream content, String contentType, long size) {
    }
}
