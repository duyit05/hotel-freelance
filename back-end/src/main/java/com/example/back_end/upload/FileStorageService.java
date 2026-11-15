package com.example.back_end.upload;

import java.io.ByteArrayInputStream;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Kiểm tra và đặt tên ảnh, lưu qua StorageBackend (MinIO hoặc local).
 * Đường dẫn trả về luôn có dạng /uploads/<tên-file>, ảnh được phục vụ bởi ImageController.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Map<String, String> ALLOWED = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp");

    /** Chỉ chữ, số, dấu chấm, gạch dưới, gạch ngang; không có "/" hay ".." */
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]*(\\.[A-Za-z0-9]+)?");

    /** Tên file do store() đặt (UUID). Ảnh mẫu như bath.jpg không khớp nên không bao giờ bị xóa */
    private static final Pattern UPLOADED_NAME = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)");

    private static final String URL_PREFIX = "/uploads/";

    private final StorageBackend backend;
    private final ImageOptimizer optimizer;

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        String contentType = file.getContentType();
        String ext = contentType == null ? null : ALLOWED.get(contentType);
        if (ext == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ nhận ảnh JPG, PNG hoặc WEBP");
        }
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // Thu nhỏ + nén (định dạng có thể đổi, vd. PNG không trong suốt -> JPEG)
        ImageOptimizer.Result optimized = optimizer.optimize(data, contentType);
        if (optimized != null) {
            data = optimized.data();
            contentType = optimized.contentType();
            ext = optimized.ext();
        }
        // Tự đặt tên file mới, không dùng tên gốc từ máy người dùng
        String name = UUID.randomUUID() + "." + ext;
        backend.put(name, new ByteArrayInputStream(data), data.length, contentType);
        return URL_PREFIX + name;
    }

    /**
     * Chuyển ảnh đã upload (đường dẫn /uploads/<tên-file>) vào thùng rác; StorageCleanupJob xóa hẳn sau vài ngày.
     * Bỏ qua link ngoài, ảnh mẫu; lỗi chỉ ghi log (file thừa không làm hỏng dữ liệu).
     */
    public void trashByUrl(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        trash(url.substring(URL_PREFIX.length()));
    }

    void trash(String name) {
        if (!isUploadedName(name)) {
            return;
        }
        try {
            backend.moveToTrash(name);
            log.info("Đã chuyển ảnh không còn dùng vào thùng rác: {}", name);
        } catch (RuntimeException e) {
            log.warn("Không chuyển được ảnh {} vào thùng rác", name, e);
        }
    }

    static String urlOf(String name) {
        return URL_PREFIX + name;
    }

    static boolean isUploadedName(String name) {
        return name != null && UPLOADED_NAME.matcher(name).matches();
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
