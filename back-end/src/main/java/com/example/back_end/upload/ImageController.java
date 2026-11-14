package com.example.back_end.upload;

import java.time.Duration;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** GET /uploads/<tên-file>: trả ảnh từ nơi lưu trữ (MinIO hoặc local). Ai cũng xem được. */
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final FileStorageService storage;

    @GetMapping("/uploads/{name}")
    public ResponseEntity<InputStreamResource> image(@PathVariable String name) {
        return storage.load(name)
            .map(file -> {
                var res = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.contentType()))
                    // Tên file không bao giờ bị ghi đè nên trình duyệt được cache lâu
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
                if (file.size() >= 0) {
                    res.contentLength(file.size());
                }
                return res.body(new InputStreamResource(file.content()));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
