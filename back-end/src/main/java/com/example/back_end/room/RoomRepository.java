package com.example.back_end.room;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
