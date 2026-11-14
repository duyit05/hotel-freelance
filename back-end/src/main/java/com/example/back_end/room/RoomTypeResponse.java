package com.example.back_end.room;

/** roomCount: số phòng đang thuộc loại này */
public record RoomTypeResponse(Long id, String name, String description, Integer sortOrder, long roomCount) {

    static RoomTypeResponse from(RoomType t, long roomCount) {
        return new RoomTypeResponse(t.getId(), t.getName(), t.getDescription(), t.getSortOrder(), roomCount);
    }
}
