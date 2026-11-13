package com.example.back_end.room;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Đường dẫn thân thiện, vd. "huong-bien" -> /phong/huong-bien */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 150)
    private String name;

    /** Giá một đêm (VNĐ) */
    @Column(nullable = false)
    private Long price;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String guests;

    @Column(length = 100)
    private String bed;

    @Column(name = "view_type", length = 100)
    private String view;

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Integer maxGuests;

    @ElementCollection
    @CollectionTable(name = "room_features", joinColumns = @JoinColumn(name = "room_id"))
    @OrderColumn(name = "position")
    @Column(name = "feature", length = 200)
    private List<String> features = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "room_highlights", joinColumns = @JoinColumn(name = "room_id"))
    @OrderColumn(name = "position")
    private List<Highlight> highlights = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "room_reviews", joinColumns = @JoinColumn(name = "room_id"))
    @OrderColumn(name = "position")
    private List<Review> reviews = new ArrayList<>();

    /** Đường dẫn ảnh, ảnh đầu tiên là ảnh bìa */
    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @OrderColumn(name = "position")
    @Column(name = "url", length = 500)
    private List<String> images = new ArrayList<>();

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Highlight {
        @Column(length = 150)
        private String title;

        @Column(name = "content", length = 500)
        private String text;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        @Column(length = 1000)
        private String quote;

        @Column(length = 150)
        private String author;
    }
}
