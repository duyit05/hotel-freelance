package com.example.back_end.room;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findBySlug(String slug);

    boolean existsBySlug(String slug);

    long countByTypeId(Long typeId);

    /** Ảnh này còn được phòng nào dùng không (một ảnh có thể dùng chung cho nhiều phòng) */
    @Query("select count(r) > 0 from Room r join r.images i where i = :url")
    boolean isImageUsed(String url);

    /** Mọi đường dẫn ảnh đang được dùng (để dọn file thừa) */
    @Query("select distinct i from Room r join r.images i")
    Set<String> findAllImageUrls();

    /** Số phòng của từng loại (chỉ các loại có ít nhất 1 phòng) */
    @Query("select r.type.id as typeId, count(r) as total from Room r where r.type is not null group by r.type.id")
    List<TypeCount> countByType();

    interface TypeCount {
        Long getTypeId();

        Long getTotal();
    }
}
