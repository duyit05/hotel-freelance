package com.example.back_end.upload;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.back_end.room.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dọn ảnh mỗi đêm lúc 3h30:
 * 1. Ảnh đã upload nhưng không phòng nào dùng (vd. upload rồi hủy form) và cũ hơn ORPHAN_MIN_AGE
 *    -> chuyển vào thùng rác. Chờ một ngày để không đụng ảnh vừa upload mà form chưa kịp lưu.
 * 2. Ảnh nằm trong thùng rác quá TRASH_RETENTION -> xóa hẳn.
 * Lỡ xóa nhầm: trong TRASH_RETENTION có thể lấy lại file trong thùng rác
 * (local: thư mục <upload-dir>/.trash, MinIO: thư mục trash/ trong bucket) chép về chỗ cũ.
 * Ảnh mẫu (bath.jpg...) không bao giờ bị dọn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageCleanupJob {

    static final Duration ORPHAN_MIN_AGE = Duration.ofDays(1);
    static final Duration TRASH_RETENTION = Duration.ofDays(7);

    private final StorageBackend backend;
    private final FileStorageService storage;
    private final RoomRepository rooms;

    @Scheduled(cron = "0 30 3 * * *")
    public void run() {
        try {
            cleanup(Instant.now());
        } catch (RuntimeException e) {
            log.warn("Lỗi khi dọn ảnh", e);
        }
    }

    void cleanup(Instant now) {
        Set<String> used = rooms.findAllImageUrls();
        int trashed = 0;
        for (StorageBackend.FileInfo f : backend.list()) {
            if (FileStorageService.isUploadedName(f.name())
                    && f.lastModified().isBefore(now.minus(ORPHAN_MIN_AGE))
                    && !used.contains(FileStorageService.urlOf(f.name()))) {
                storage.trash(f.name());
                trashed++;
            }
        }

        int deleted = 0;
        for (StorageBackend.FileInfo f : backend.listTrash()) {
            if (f.lastModified().isBefore(now.minus(TRASH_RETENTION))) {
                try {
                    backend.deleteFromTrash(f.name());
                    deleted++;
                } catch (RuntimeException e) {
                    log.warn("Không xóa được {} trong thùng rác", f.name(), e);
                }
            }
        }
        if (trashed > 0 || deleted > 0) {
            log.info("Dọn ảnh: {} ảnh thừa vào thùng rác, xóa hẳn {} ảnh trong thùng rác", trashed, deleted);
        }
    }
}
