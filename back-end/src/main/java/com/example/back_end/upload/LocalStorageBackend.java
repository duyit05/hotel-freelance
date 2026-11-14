package com.example.back_end.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.back_end.config.AppProperties;

/** Lưu ảnh vào thư mục app.upload-dir trên máy chạy back-end (app.storage=local, dùng cho test/dev) */
@Component
@ConditionalOnProperty(prefix = "app", name = "storage", havingValue = "local")
public class LocalStorageBackend implements StorageBackend {

    private final Path root;

    public LocalStorageBackend(AppProperties props) {
        this.root = Path.of(props.uploadDir()).toAbsolutePath().normalize();
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
}
