package com.example.back_end.room;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.back_end.common.FieldValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomTypeService {

    static final Sort ORDER = Sort.by("sortOrder", "id");

    private final RoomTypeRepository types;
    private final RoomRepository rooms;

    @Transactional(readOnly = true)
    public List<RoomTypeResponse> findAll() {
        Map<Long, Long> counts = rooms.countByType().stream()
            .collect(Collectors.toMap(RoomRepository.TypeCount::getTypeId, RoomRepository.TypeCount::getTotal));
        return types.findAll(ORDER).stream()
            .map(t -> RoomTypeResponse.from(t, counts.getOrDefault(t.getId(), 0L)))
            .toList();
    }

    @Transactional(readOnly = true)
    public RoomTypeResponse findById(Long id) {
        RoomType type = get(id);
        return RoomTypeResponse.from(type, rooms.countByTypeId(id));
    }

    public RoomTypeResponse create(RoomTypeRequest req) {
        ensureNameFree(req.name(), null);
        RoomType type = new RoomType();
        req.applyTo(type);
        return RoomTypeResponse.from(types.save(type), 0);
    }

    public RoomTypeResponse update(Long id, RoomTypeRequest req) {
        RoomType type = get(id);
        ensureNameFree(req.name(), id);
        req.applyTo(type);
        return RoomTypeResponse.from(type, rooms.countByTypeId(id));
    }

    public void delete(Long id) {
        RoomType type = get(id);
        long used = rooms.countByTypeId(id);
        if (used > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Đang có " + used + " phòng thuộc loại \"" + type.getName() + "\". Hãy chuyển các phòng đó sang loại khác trước khi xóa.");
        }
        types.delete(type);
    }

    /** Dùng khi lưu phòng: đổi id loại phòng thành entity */
    RoomType get(Long id) {
        return types.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng"));
    }

    private void ensureNameFree(String name, Long selfId) {
        types.findByNameIgnoreCase(name.trim())
            .filter(t -> !t.getId().equals(selfId))
            .ifPresent(t -> {
                throw new FieldValidationException(Map.of("name", "Đã có loại phòng tên \"" + t.getName() + "\""));
            });
    }

    /** Tra cứu nhanh theo tên (không phân biệt hoa thường), dùng khi nạp dữ liệu mẫu */
    @Transactional(readOnly = true)
    public Map<String, RoomType> byLowerName() {
        return types.findAll().stream().collect(Collectors.toMap(t -> t.getName().toLowerCase(), Function.identity()));
    }
}
