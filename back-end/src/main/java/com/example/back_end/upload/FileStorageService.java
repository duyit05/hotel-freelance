package com.example.back_end.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.back_end.config.AppProperties;

/** Lưu ảnh vào thư mục app.upload-dir, trả về đường dẫn dạng /uploads/<tên-file> */
@Service
public class FileStorageService {

    private static final Map<String, String> ALLOWED = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp");

    private final Path root;

    public FileStorageService(AppProperties props) {
        this.root = Path.of(props.uploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Không tạo được thư mục upload: " + root, e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ nhận ảnh JPG, PNG hoặc WEBP");
        }
        // Tự đặt tên file mới, không dùng tên gốc từ máy người dùng
        String name = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return "/uploads/" + name;
    }

    /** Dùng khi khởi tạo dữ liệu mẫu */
    public void storeIfMissing(String name, InputStream in) throws IOException {
        Path target = root.resolve(name).normalize();
        if (!target.startsWith(root) || Files.exists(target)) {
            return;
        }
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
