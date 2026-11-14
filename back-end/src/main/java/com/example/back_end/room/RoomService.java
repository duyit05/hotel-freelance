package com.example.back_end.room;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository rooms;
    private final RoomTypeService types;

    @Transactional(readOnly = true)
    public List<RoomResponse> findAll() {
        return rooms.findAll(Sort.by("id")).stream().map(RoomResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        return RoomResponse.from(get(id));
    }

    @Transactional(readOnly = true)
    public RoomResponse findBySlug(String slug) {
        return rooms.findBySlug(slug).map(RoomResponse::from).orElseThrow(RoomService::notFound);
    }

    /** Không gửi slug thì tự tạo từ tên phòng: "Phòng hướng biển" -> "phong-huong-bien" (trùng thì thêm -2, -3...) */
    public RoomResponse create(RoomRequest req) {
        Room room = new Room();
        if (hasText(req.slug())) {
            ensureSlugFree(req.slug());
            room.setSlug(req.slug());
        } else {
            room.setSlug(uniqueSlug(slugify(req.name())));
        }
        req.applyTo(room, types.get(req.typeId()));
        return RoomResponse.from(rooms.save(room));
    }

    /** Khi sửa, giữ nguyên đường dẫn cũ để link khách đã lưu/chia sẻ không bị hỏng */
    public RoomResponse update(Long id, RoomRequest req) {
        Room room = get(id);
        if (hasText(req.slug()) && !room.getSlug().equals(req.slug())) {
            ensureSlugFree(req.slug());
            room.setSlug(req.slug());
        }
        req.applyTo(room, types.get(req.typeId()));
        return RoomResponse.from(room);
    }

    private String uniqueSlug(String base) {
        String slug = base;
        for (int i = 2; rooms.existsBySlug(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    static String slugify(String text) {
        String s = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd').replace('Đ', 'D')
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        if (s.length() > 90) {
            s = s.substring(0, 90).replaceAll("-+$", "");
        }
        return s.isEmpty() ? "phong" : s;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public void delete(Long id) {
        rooms.delete(get(id));
    }

    private Room get(Long id) {
        return rooms.findById(id).orElseThrow(RoomService::notFound);
    }

    private void ensureSlugFree(String slug) {
        if (rooms.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đường dẫn \"" + slug + "\" đã được dùng cho phòng khác");
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng");
    }
}
