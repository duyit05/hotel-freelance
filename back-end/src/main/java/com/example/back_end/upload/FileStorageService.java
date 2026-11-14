package com.example.back_end.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

/**
 * Kiểm tra và đặt tên ảnh, lưu qua StorageBackend (MinIO hoặc local).
 * Đường dẫn trả về luôn có dạng /uploads/<tên-file>, ảnh được phục vụ bởi ImageController.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Map<String, String> ALLOWED = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp");

    /** Chỉ chữ, số, dấu chấm, gạch dưới, gạch ngang; không có "/" hay ".." */
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]*(\\.[A-Za-z0-9]+)?");

    private final StorageBackend backend;

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        String contentType = file.getContentType();
        String ext = contentType == null ? null : ALLOWED.get(contentType);
        if (ext == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ nhận ảnh JPG, PNG hoặc WEBP");
        }
        // Tự đặt tên file mới, không dùng tên gốc từ máy người dùng
        String name = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            backend.put(name, in, file.getSize(), contentType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return "/uploads/" + name;
    }

    /** Dùng khi khởi tạo dữ liệu mẫu */
    public void storeIfMissing(String name, InputStream in) {
        if (!isValidName(name) || backend.exists(name)) {
            return;
        }
        backend.put(name, in, -1, contentTypeOf(name));
    }

    public Optional<StorageBackend.StoredFile> load(String name) {
        return isValidName(name) ? backend.get(name) : Optional.empty();
    }

    static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    static String contentTypeOf(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
