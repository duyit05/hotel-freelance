package com.example.back_end.room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Loại phòng (Tiêu chuẩn, Cao cấp, Gia đình...), admin tự thêm/sửa/xóa */
@Entity
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** Thứ tự hiển thị, số nhỏ đứng trước */
    @Column(nullable = false)
    private Integer sortOrder = 0;

    public RoomType(String name, String description, int sortOrder) {
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
    }
}
