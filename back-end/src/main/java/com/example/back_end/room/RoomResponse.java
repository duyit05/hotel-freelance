package com.example.back_end.room;

import java.util.List;

import com.example.back_end.room.RoomRequest.HighlightDto;
import com.example.back_end.room.RoomRequest.ReviewDto;

public record RoomResponse(
    Long id,
    String slug,
    String name,
    TypeRef type,
    Long price,
    String size,
    String guests,
    String bed,
    String view,
    String shortDescription,
    String description,
    Integer maxGuests,
    List<String> features,
    List<HighlightDto> highlights,
    List<ReviewDto> reviews,
    List<String> images) {

    /** Loại phòng rút gọn đi kèm mỗi phòng */
    public record TypeRef(Long id, String name) {
    }

    static RoomResponse from(Room r) {
        RoomType t = r.getType();
        return new RoomResponse(
            r.getId(), r.getSlug(), r.getName(), t == null ? null : new TypeRef(t.getId(), t.getName()),
            r.getPrice(), r.getSize(), r.getGuests(), r.getBed(), r.getView(),
            r.getShortDescription(), r.getDescription(), r.getMaxGuests(),
            List.copyOf(r.getFeatures()),
            r.getHighlights().stream().map(h -> new HighlightDto(h.getTitle(), h.getText())).toList(),
            r.getReviews().stream().map(v -> new ReviewDto(v.getQuote(), v.getAuthor())).toList(),
            List.copyOf(r.getImages()));
    }
}
