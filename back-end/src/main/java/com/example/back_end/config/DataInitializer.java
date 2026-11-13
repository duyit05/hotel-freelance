package com.example.back_end.config;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.back_end.room.RoomRepository;
import com.example.back_end.room.RoomRequest;
import com.example.back_end.room.RoomService;
import com.example.back_end.upload.FileStorageService;
import com.example.back_end.user.Role;
import com.example.back_end.user.User;
import com.example.back_end.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Chạy một lần khi khởi động:
 * - Tạo tài khoản admin nếu chưa có (lấy từ app.admin.*).
 * - Nếu bảng phòng đang trống: chép ảnh mẫu vào thư mục upload và thêm 6 phòng mẫu từ seed/rooms.json.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AppProperties props;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final FileStorageService storage;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedAdmin();
        seedRooms();
    }

    private void seedAdmin() {
        String username = props.admin().username();
        if (users.existsByUsername(username)) {
            return;
        }
        users.save(new User(username, passwordEncoder.encode(props.admin().password()), Role.ADMIN));
        log.info("Đã tạo tài khoản admin '{}'", username);
    }

    private void seedRooms() throws Exception {
        if (roomRepository.count() > 0) {
            return;
        }
        var resolver = new PathMatchingResourcePatternResolver();
        for (Resource image : resolver.getResources("classpath:seed/images/*")) {
            try (InputStream in = image.getInputStream()) {
                storage.storeIfMissing(image.getFilename(), in);
            }
        }
        try (InputStream in = resolver.getResource("classpath:seed/rooms.json").getInputStream()) {
            List<RoomRequest> rooms = objectMapper.readValue(in, new TypeReference<>() {
            });
            rooms.forEach(roomService::create);
            log.info("Đã thêm {} phòng mẫu", rooms.size());
        }
    }
}
