package com.example.back_end.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.back_end.config.AppProperties;

/** Lưu ảnh vào thư mục app.upload-dir trên máy chạy back-end (app.storage=local, mặc định) */
@Component
@ConditionalOnProperty(prefix = "app", name = "storage", havingValue = "local", matchIfMissing = true)
public class LocalStorageBackend implements StorageBackend {

    private final Path root;
    /** Thùng rác: thư mục con ".trash" (tên bắt đầu bằng dấu chấm nên ImageController không phục vụ) */
    private final Path trash;

    public LocalStorageBackend(AppProperties props) {
        this.root = Path.of(props.uploadDir()).toAbsolutePath().normalize();
        this.trash = root.resolve(".trash");
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Không tạo được thư mục upload: " + root, e);
        }
    }

    @Override
    public void put(String name, InputStream in, long size, String contentType) {
        try {
            Files.copy(in, root.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean exists(String name) {
        return Files.exists(root.resolve(name));
    }

    @Override
    public Optional<StoredFile> get(String name) {
        Path file = root.resolve(name);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new StoredFile(Files.newInputStream(file),
                FileStorageService.contentTypeOf(name), Files.size(file)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<FileInfo> list() {
        return listDir(root);
    }

    @Override
    public void moveToTrash(String name) {
        Path file = root.resolve(name);
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            Files.createDirectories(trash);
            Path target = trash.resolve(name);
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            // Đánh dấu thời điểm bỏ vào thùng rác để tính hạn xóa hẳn
            Files.setLastModifiedTime(target, FileTime.from(Instant.now()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<FileInfo> listTrash() {
        return Files.isDirectory(trash) ? listDir(trash) : List.of();
    }

    @Override
    public void deleteFromTrash(String name) {
        try {
            Files.deleteIfExists(trash.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<FileInfo> listDir(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            List<FileInfo> result = new ArrayList<>();
            for (Path f : files.filter(Files::isRegularFile).toList()) {
                result.add(new FileInfo(f.getFileName().toString(), Files.getLastModifiedTime(f).toInstant()));
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
