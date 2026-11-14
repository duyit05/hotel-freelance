package com.example.back_end.config;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.back_end.room.RoomRepository;
import com.example.back_end.room.RoomRequest;
import com.example.back_end.room.RoomService;
import com.example.back_end.room.RoomType;
import com.example.back_end.room.RoomTypeRepository;
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
 * - Tạo tài khoản admin nếu chưa có (lấy từ app.admin.*), điền họ tên cho tài khoản cũ chưa có.
 * - Tạo loại phòng mặc định nếu chưa có, chuyển dữ liệu loại phòng cũ (nếu có).
 * - Nếu bảng phòng đang trống: chép ảnh mẫu vào nơi lưu ảnh (MinIO hoặc thư mục local) và thêm 6 phòng mẫu từ seed/rooms.json.
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
    private final RoomTypeRepository roomTypeRepository;
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedAdmin();
        seedRoomTypes();
        migrateRoomTypes();
        seedRooms();
    }

    private void seedAdmin() {
        String username = props.admin().username();
        String fullName = props.admin().fullName();
        if (!users.existsByUsername(username)) {
            users.save(new User(username, fullName, passwordEncoder.encode(props.admin().password()), Role.ADMIN));
            log.info("Đã tạo tài khoản admin '{}'", username);
        }
        // Tài khoản tạo trước khi có cột full_name: điền tạm để không bị trống
        for (User user : users.findByFullNameIsNull()) {
            user.setFullName(user.getUsername().equals(username) ? fullName : user.getUsername());
            users.save(user);
            log.info("Đã điền họ tên cho tài khoản '{}'", user.getUsername());
        }
    }


    /* ---------- Loại phòng ---------- */

    /** Loại phòng mặc định khi bảng còn trống (admin sửa/xóa/thêm được trong trang quản trị) */
    private static final List<String[]> DEFAULT_TYPES = List.of(
        new String[] { "Phòng tiêu chuẩn", "Phòng cơ bản, đầy đủ tiện nghi, giá tốt." },
        new String[] { "Phòng Superior", "Rộng hơn phòng tiêu chuẩn, nội thất nâng cấp." },
        new String[] { "Phòng cao cấp (Deluxe)", "Không gian rộng, tầm nhìn đẹp, tiện nghi cao cấp." },
        new String[] { "Phòng Suite", "Có phòng khách riêng, phù hợp kỳ nghỉ dài ngày." },
        new String[] { "Phòng gia đình", "Nhiều giường, đủ chỗ cho gia đình có trẻ nhỏ." },
        new String[] { "Phòng thông nhau", "Hai phòng có cửa thông nhau, hợp với nhóm bạn." });

    /** Mã loại phòng ở phiên bản trước (cột rooms.type) -> tên loại phòng mới */
    private static final Map<String, String> LEGACY_CODES = Map.of(
        "STANDARD", "Phòng tiêu chuẩn",
        "SUPERIOR", "Phòng Superior",
        "DELUXE", "Phòng cao cấp (Deluxe)",
        "SUITE", "Phòng Suite",
        "FAMILY", "Phòng gia đình",
        "CONNECTING", "Phòng thông nhau",
        "PRESIDENTIAL", "Phòng Tổng thống");

    private void seedRoomTypes() {
        if (roomTypeRepository.count() > 0) {
            return;
        }
        for (int i = 0; i < DEFAULT_TYPES.size(); i++) {
            String[] t = DEFAULT_TYPES.get(i);
            roomTypeRepository.save(new RoomType(t[0], t[1], (i + 1) * 10));
        }
        log.info("Đã thêm {} loại phòng mặc định", DEFAULT_TYPES.size());
    }

    private RoomType typeNamed(String name) {
        return roomTypeRepository.findByNameIgnoreCase(name)
            .orElseGet(() -> roomTypeRepository.save(new RoomType(name, null, 900)));
    }

    /**
     * Phòng tạo trước khi có bảng loại phòng:
     * - Có cột cũ rooms.type (mã DELUXE, FAMILY...): chuyển sang loại phòng tương ứng rồi xóa cột cũ.
     * - Phòng còn lại chưa có loại: gán "Phòng tiêu chuẩn" để admin sửa sau.
     */
    private void migrateRoomTypes() {
        Integer legacy = jdbc.queryForObject(
            "select count(*) from information_schema.columns where lower(table_name) = 'rooms' and lower(column_name) = 'type'",
            Integer.class);
        if (legacy != null && legacy > 0) {
            List<String> codes = jdbc.queryForList(
                "select distinct type from rooms where room_type_id is null and type is not null", String.class);
            for (String code : codes) {
                String name = LEGACY_CODES.getOrDefault(code, code);
                jdbc.update("update rooms set room_type_id = ? where room_type_id is null and type = ?",
                    typeNamed(name).getId(), code);
            }
            jdbc.execute("alter table rooms drop column type");
            log.info("Đã chuyển cột loại phòng cũ sang bảng room_types");
        }
        Integer untyped = jdbc.queryForObject("select count(*) from rooms where room_type_id is null", Integer.class);
        if (untyped != null && untyped > 0) {
            jdbc.update("update rooms set room_type_id = ? where room_type_id is null",
                typeNamed(DEFAULT_TYPES.get(0)[0]).getId());
            log.info("Đã gán loại phòng mặc định cho {} phòng", untyped);
        }
    }

    /* ---------- Phòng mẫu ---------- */

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
        List<Map<String, Object>> seed;
        try (InputStream in = resolver.getResource("classpath:seed/rooms.json").getInputStream()) {
            seed = objectMapper.readValue(in, new TypeReference<>() {
            });
        }
        // Trong file mẫu, "type" là tên loại phòng; API cần id
        for (Map<String, Object> room : seed) {
            room.put("typeId", typeNamed((String) room.remove("type")).getId());
            roomService.create(objectMapper.convertValue(room, RoomRequest.class));
        }
        log.info("Đã thêm {} phòng mẫu", seed.size());
    }
}
