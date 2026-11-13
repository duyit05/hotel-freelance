package com.example.back_end.room;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * GET    /api/rooms              danh sách phòng (công khai)
 * GET    /api/rooms/slug/{slug}  chi tiết theo đường dẫn (công khai, dùng cho trang /phong/{slug})
 * GET    /api/rooms/{id}         chi tiết theo id (công khai)
 * POST   /api/rooms              thêm phòng (ADMIN)
 * PUT    /api/rooms/{id}         sửa phòng (ADMIN)
 * DELETE /api/rooms/{id}         xóa phòng (ADMIN)
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService service;

    @GetMapping
    public List<RoomResponse> list() {
        return service.findAll();
    }

    @GetMapping("/slug/{slug}")
    public RoomResponse bySlug(@PathVariable String slug) {
        return service.findBySlug(slug);
    }

    @GetMapping("/{id}")
    public RoomResponse byId(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
