package com.example.back_end.room;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu admin gửi lên khi thêm/sửa phòng */
public record RoomRequest(
    /** Không bắt buộc: bỏ trống thì hệ thống tự tạo từ tên phòng (thêm mới) hoặc giữ nguyên (khi sửa) */
    @Size(max = 100)
    @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "Chỉ dùng chữ thường không dấu, số và dấu gạch ngang")
    String slug,

    @NotBlank(message = "Vui lòng nhập tên phòng") @Size(max = 150) String name,
    @NotNull(message = "Vui lòng chọn loại phòng") Long typeId,
    @NotNull(message = "Vui lòng nhập giá") @Min(value = 0, message = "Giá không được âm") Long price,
    @Size(max = 50) String size,
    @Size(max = 50) String guests,
    @Size(max = 100) String bed,
    @Size(max = 100) String view,
    @Size(max = 500) String shortDescription,
    String description,

    @NotNull(message = "Vui lòng nhập số khách tối đa")
    @Min(value = 1, message = "Ít nhất 1 khách")
    @Max(value = 20, message = "Tối đa 20 khách")
    Integer maxGuests,

    List<@NotBlank @Size(max = 200) String> features,
    List<@Valid HighlightDto> highlights,
    List<@Valid ReviewDto> reviews,
    List<@NotBlank @Size(max = 500) String> images) {

    public record HighlightDto(@NotBlank @Size(max = 150) String title, @Size(max = 500) String text) {
    }

    public record ReviewDto(@NotBlank @Size(max = 1000) String quote, @Size(max = 150) String author) {
    }

    /** Ghi dữ liệu vào entity, trừ slug do RoomService xử lý (giữ nguyên các collection để Hibernate cập nhật đúng) */
    void applyTo(Room room, RoomType type) {
        room.setName(name);
        room.setType(type);
        room.setPrice(price);
        room.setSize(size);
        room.setGuests(guests);
        room.setBed(bed);
        room.setView(view);
        room.setShortDescription(shortDescription);
        room.setDescription(description);
        room.setMaxGuests(maxGuests);
        replace(room.getFeatures(), features);
        replace(room.getImages(), images);
        replace(room.getHighlights(), highlights == null ? null
            : highlights.stream().map(h -> new Room.Highlight(h.title(), h.text())).toList());
        replace(room.getReviews(), reviews == null ? null
            : reviews.stream().map(r -> new Room.Review(r.quote(), r.author())).toList());
    }

    private static <T> void replace(List<T> target, List<T> source) {
        target.clear();
        if (source != null) {
            target.addAll(source);
        }
    }
}
