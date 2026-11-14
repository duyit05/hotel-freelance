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
 * GET    /api/room-types       danh sách loại phòng kèm số phòng (công khai)
 * GET    /api/room-types/{id}  chi tiết (công khai)
 * POST   /api/room-types       thêm (ADMIN)
 * PUT    /api/room-types/{id}  sửa (ADMIN)
 * DELETE /api/room-types/{id}  xóa, chỉ khi không còn phòng nào thuộc loại này (ADMIN)
 */
@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService service;

    @GetMapping
    public List<RoomTypeResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RoomTypeResponse byId(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomTypeResponse create(@Valid @RequestBody RoomTypeRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public RoomTypeResponse update(@PathVariable Long id, @Valid @RequestBody RoomTypeRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
