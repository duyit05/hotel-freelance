package com.example.back_end.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dữ liệu admin gửi lên khi thêm/sửa loại phòng */
public record RoomTypeRequest(
    @NotBlank(message = "Vui lòng nhập tên loại phòng") @Size(max = 100, message = "Tối đa 100 ký tự") String name,
    @Size(max = 500, message = "Tối đa 500 ký tự") String description,
    @Min(value = 0, message = "Không được âm") @Max(value = 999, message = "Tối đa 999") Integer sortOrder) {

    void applyTo(RoomType type) {
        type.setName(name.trim());
        type.setDescription(description == null || description.isBlank() ? null : description.trim());
        type.setSortOrder(sortOrder == null ? 0 : sortOrder);
    }
}
