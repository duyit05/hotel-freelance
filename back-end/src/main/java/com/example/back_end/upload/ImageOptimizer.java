package com.example.back_end.upload;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Thu nhỏ và nén ảnh trước khi lưu, để tiết kiệm ổ đĩa VPS và trang web tải nhanh hơn.
 * - Cạnh dài tối đa MAX_SIDE px (đủ nét cho màn hình lớn), JPEG chất lượng JPEG_QUALITY.
 *   Ảnh điện thoại 4–8 MB thường còn khoảng 300–600 KB.
 * - Tự xoay theo EXIF và bỏ toàn bộ metadata (vị trí GPS, thông tin máy chụp...).
 * - Kiểm tra nội dung thật là ảnh, chặn ảnh có số điểm ảnh quá lớn (giải nén ra hàng GB RAM).
 * - PNG có nền trong suốt giữ PNG; PNG không trong suốt chuyển sang JPEG.
 * - WEBP: Java không đọc được nên lưu nguyên bản.
 */
@Slf4j
@Component
public class ImageOptimizer {

    static final int MAX_SIDE = 1920;
    static final float JPEG_QUALITY = 0.82f;
    /** 40 triệu điểm ảnh ≈ 160 MB RAM khi giải nén */
    static final long MAX_PIXELS = 40_000_000L;

    /** Xử lý từng ảnh một để không vượt RAM của VPS nhỏ khi upload nhiều ảnh cùng lúc */
    private final Semaphore permits = new Semaphore(1, true);

    public record Result(byte[] data, String contentType, String ext) {
    }

    /** Trả về null nếu định dạng không xử lý được (WEBP) -> người gọi lưu nguyên bản */
    public Result optimize(byte[] original, String contentType) {
        if ("image/webp".equals(contentType)) {
            return null;
        }
        checkDimensions(original);
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Máy chủ đang bận, vui lòng thử lại");
        }
        try {
            // Đọc qua Thumbnailator để ảnh được xoay đúng theo EXIF
            BufferedImage img = Thumbnails.of(new ByteArrayInputStream(original)).scale(1).asBufferedImage();
            boolean keepPng = "image/png".equals(contentType) && img.getColorModel().hasAlpha();
            String format = keepPng ? "png" : "jpg";

            var builder = Thumbnails.of(img);
            if (Math.max(img.getWidth(), img.getHeight()) > MAX_SIDE) {
                builder.size(MAX_SIDE, MAX_SIDE);
            } else {
                builder.scale(1);
            }
            builder.outputFormat(format);
            if (!keepPng) {
                builder.outputQuality(JPEG_QUALITY);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.toOutputStream(out);
            byte[] data = out.toByteArray();
            log.info("Nén ảnh {}x{}: {} KB -> {} KB", img.getWidth(), img.getHeight(),
                original.length / 1024, data.length / 1024);
            return new Result(data, keepPng ? "image/png" : "image/jpeg", format);
        } catch (IOException e) {
            throw invalid();
        } finally {
            permits.release();
        }
    }

    /** Chỉ đọc phần đầu file để lấy kích thước, chưa giải nén ảnh */
    private static void checkDimensions(byte[] data) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            Iterator<ImageReader> readers = in == null ? null : ImageIO.getImageReaders(in);
            if (readers == null || !readers.hasNext()) {
                throw invalid();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > MAX_PIXELS) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ảnh có độ phân giải quá lớn, vui lòng dùng ảnh nhỏ hơn 40 megapixel");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw invalid();
        }
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "File không phải ảnh hợp lệ");
    }
}
