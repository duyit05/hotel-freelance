package com.example.back_end.room;

import java.util.List;

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

    public RoomResponse create(RoomRequest req) {
        ensureSlugFree(req.slug());
        Room room = new Room();
        req.applyTo(room);
        return RoomResponse.from(rooms.save(room));
    }

    public RoomResponse update(Long id, RoomRequest req) {
        Room room = get(id);
        if (!room.getSlug().equals(req.slug())) {
            ensureSlugFree(req.slug());
        }
        req.applyTo(room);
        return RoomResponse.from(room);
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
