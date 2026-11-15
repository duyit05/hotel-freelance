package com.example.back_end;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackEndApplicationTests {

	private static final String NEW_ROOM = """
		{ "slug": "phong-test", "name": "Phòng test", "typeId": TYPE_ID, "price": 500000, "maxGuests": 2,
		  "features": ["Wi-Fi"], "highlights": [{"title": "A", "text": "B"}],
		  "reviews": [], "images": [] }
		""";

	@Autowired
	MockMvc mvc;

	@Test
	void publicCanListSeededRooms() throws Exception {
		mvc.perform(get("/api/rooms"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(6))
			.andExpect(jsonPath("$[0].slug").value("deluxe"));
		mvc.perform(get("/api/rooms/slug/huong-bien")).andExpect(status().isOk());
		mvc.perform(get("/api/rooms/slug/khong-co")).andExpect(status().isNotFound());
	}

	@Test
	void writeRequiresToken() throws Exception {
		mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(NEW_ROOM))
			.andExpect(status().isUnauthorized());
		mvc.perform(delete("/api/rooms/1").header("Authorization", "Bearer token-gia"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void wrongPasswordIsRejected() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"sai\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Sai tên đăng nhập hoặc mật khẩu"));
	}

	@Test
	void adminCanLoginAndCrudRooms() throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.roles[0]").value("ADMIN"))
			.andReturn().getResponse().getContentAsString();
		String auth = "Bearer " + JsonPath.read(body, "$.token");

		mvc.perform(get("/api/auth/me").header("Authorization", auth))
			.andExpect(jsonPath("$.username").value("admin"));

		Integer typeId = JsonPath.read(mvc.perform(get("/api/room-types")).andReturn().getResponse().getContentAsString(), "$[0].id");
		String newRoom = NEW_ROOM.replace("TYPE_ID", typeId.toString());

		String created = mvc.perform(post("/api/rooms").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(newRoom))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.highlights[0].title").value("A"))
			.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(created, "$.id");

		// Trùng đường dẫn -> 409
		mvc.perform(post("/api/rooms").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(newRoom))
			.andExpect(status().isConflict());

		// Không gửi đường dẫn -> tự tạo từ tên, trùng thì thêm -2
		String noSlug = newRoom.replace("\"slug\": \"phong-test\", ", "");
		String auto = mvc.perform(post("/api/rooms").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(noSlug))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.slug").value("phong-test-2"))
			.andReturn().getResponse().getContentAsString();
		mvc.perform(delete("/api/rooms/" + JsonPath.read(auto, "$.id")).header("Authorization", auth))
			.andExpect(status().isNoContent());

		// Thiếu tên -> 400 kèm lỗi theo từng trường
		mvc.perform(post("/api/rooms").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"x\",\"price\":1,\"maxGuests\":1}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.name").exists());

		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/rooms/" + id)
				.header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(noSlug.replace("Phòng test", "Phòng đã sửa")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Phòng đã sửa"))
			.andExpect(jsonPath("$.slug").value("phong-test")); // sửa tên không đổi đường dẫn

		mvc.perform(delete("/api/rooms/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
		assertThat(mvc.perform(get("/api/rooms/" + id)).andReturn().getResponse().getStatus()).isEqualTo(404);
	}

	private String login(String username, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(body, "$.token");
	}

	private static final String REFRESH_COOKIE = com.example.back_end.auth.AuthController.REFRESH_COOKIE;

	private org.springframework.test.web.servlet.ResultActions refresh(String refreshToken) throws Exception {
		return mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "XMLHttpRequest")
			.cookie(new jakarta.servlet.http.Cookie(REFRESH_COOKIE, refreshToken)));
	}

	@Test
	void refreshTokenRotatesAndDetectsReuse() throws Exception {
		var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse();
		var cookie = login.getCookie(REFRESH_COOKIE);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.getSecure()).isTrue();
		assertThat(cookie.getPath()).isEqualTo("/api/auth");
		assertThat(login.getHeader("Set-Cookie")).contains("SameSite=Strict");
		String first = cookie.getValue();

		// Thiếu header X-Requested-With -> từ chối (chống CSRF)
		mvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie(REFRESH_COOKIE, first)))
			.andExpect(status().isForbidden());

		// Làm mới: được access token mới + refresh token mới
		var res = refresh(first).andExpect(status().isOk()).andReturn().getResponse();
		String second = res.getCookie(REFRESH_COOKIE).getValue();
		assertThat(second).isNotEqualTo(first);
		mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + JsonPath.read(res.getContentAsString(), "$.token")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("admin"));

		// Token cũ gửi lại ngay (vd. tab khác): vẫn được access token nhưng không đổi cookie
		var grace = refresh(first).andExpect(status().isOk()).andReturn().getResponse();
		assertThat(grace.getCookie(REFRESH_COOKIE)).isNull();

		// Đăng xuất -> thu hồi cả phiên, token nào của phiên đó cũng hết dùng được
		mvc.perform(post("/api/auth/logout").header("X-Requested-With", "XMLHttpRequest")
				.cookie(new jakarta.servlet.http.Cookie(REFRESH_COOKIE, second)))
			.andExpect(status().isNoContent());
		refresh(second).andExpect(status().isUnauthorized());
		refresh(first).andExpect(status().isUnauthorized());
		refresh("token-gia").andExpect(status().isUnauthorized());
	}

	@Test
	void tooManyFailedLoginsAreBlocked() throws Exception {
		for (int i = 0; i < 5; i++) {
			mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"khoa-thu\",\"password\":\"sai\"}"))
				.andExpect(status().isUnauthorized());
		}
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"khoa-thu\",\"password\":\"sai\"}"))
			.andExpect(status().isTooManyRequests());
	}

	/** Tạo ảnh thật kích thước w x h (alpha = có nền trong suốt) */
	private static byte[] image(String format, int w, int h, boolean alpha) throws Exception {
		var img = new java.awt.image.BufferedImage(w, h,
			alpha ? java.awt.image.BufferedImage.TYPE_INT_ARGB : java.awt.image.BufferedImage.TYPE_INT_RGB);
		var g = img.createGraphics();
		g.setColor(java.awt.Color.ORANGE);
		g.fillRect(0, 0, w / 2, h / 2);
		g.dispose();
		var out = new java.io.ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, format, out);
		return out.toByteArray();
	}

	private String uploadPng(String auth) throws Exception {
		byte[] png = image("png", 40, 30, false);
		var file = new org.springframework.mock.web.MockMultipartFile("file", "a.png", "image/png", png);
		String body = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/uploads")
				.file(file).header("Authorization", auth))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.url");
	}

	@Test
	void removedImagesAreDeletedFromStorage() throws Exception {
		String auth = login("admin", "admin123");
		String kept = uploadPng(auth);
		String removed = uploadPng(auth);
		String shared = uploadPng(auth);
		Integer typeId = JsonPath.read(mvc.perform(get("/api/room-types")).andReturn().getResponse().getContentAsString(), "$[0].id");
		String room = NEW_ROOM.replace("TYPE_ID", typeId.toString());

		// Hai phòng cùng dùng ảnh "shared"
		Integer a = JsonPath.read(mvc.perform(post("/api/rooms").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(room.replace("phong-test", "anh-a").replace("\"images\": []",
					"\"images\": [\"" + kept + "\", \"" + removed + "\", \"" + shared + "\", \"/uploads/bath.jpg\"]")))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
		Integer b = JsonPath.read(mvc.perform(post("/api/rooms").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(room.replace("phong-test", "anh-b").replace("\"images\": []", "\"images\": [\"" + shared + "\"]")))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");

		// Sửa phòng A, bỏ "removed": file bị xóa, ảnh khác còn nguyên
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/rooms/" + a)
				.header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(room.replace("phong-test", "anh-a").replace("\"images\": []",
					"\"images\": [\"" + kept + "\", \"" + shared + "\", \"/uploads/bath.jpg\"]")))
			.andExpect(status().isOk());
		mvc.perform(get(removed)).andExpect(status().isNotFound());
		mvc.perform(get(kept)).andExpect(status().isOk());
		// Không xóa hẳn ngay mà nằm trong thùng rác
		assertThat(TRASH.resolve(removed.substring("/uploads/".length()))).exists();

		// Xóa phòng A: "kept" bị xóa; "shared" còn phòng B dùng nên giữ; ảnh mẫu không bao giờ bị xóa
		mvc.perform(delete("/api/rooms/" + a).header("Authorization", auth)).andExpect(status().isNoContent());
		mvc.perform(get(kept)).andExpect(status().isNotFound());
		mvc.perform(get(shared)).andExpect(status().isOk());
		mvc.perform(get("/uploads/bath.jpg")).andExpect(status().isOk());

		mvc.perform(delete("/api/rooms/" + b).header("Authorization", auth)).andExpect(status().isNoContent());
		mvc.perform(get(shared)).andExpect(status().isNotFound());
	}

	private static final java.nio.file.Path TRASH = java.nio.file.Path.of("target/test-uploads/.trash");

	@Autowired
	com.example.back_end.upload.StorageCleanupJob cleanupJob;

	@Test
	void nightlyCleanupTrashesOrphansThenPurges() throws Exception {
		String auth = login("admin", "admin123");
		String orphan = uploadPng(auth); // upload rồi không lưu vào phòng nào
		String orphanName = orphan.substring("/uploads/".length());
		String roomImage = JsonPath.read(mvc.perform(get("/api/rooms")).andReturn().getResponse().getContentAsString(), "$[0].images[0]");

		// Mới upload: chưa đụng tới
		invokeCleanup(java.time.Instant.now());
		mvc.perform(get(orphan)).andExpect(status().isOk());

		// Sau 2 ngày: ảnh thừa vào thùng rác, ảnh đang dùng và ảnh mẫu còn nguyên
		invokeCleanup(java.time.Instant.now().plus(java.time.Duration.ofDays(2)));
		mvc.perform(get(orphan)).andExpect(status().isNotFound());
		assertThat(TRASH.resolve(orphanName)).exists();
		mvc.perform(get(roomImage)).andExpect(status().isOk());
		mvc.perform(get("/uploads/bath.jpg")).andExpect(status().isOk());

		// Quá 7 ngày trong thùng rác: xóa hẳn
		invokeCleanup(java.time.Instant.now().plus(java.time.Duration.ofDays(9)));
		assertThat(TRASH.resolve(orphanName)).doesNotExist();
	}

	private void invokeCleanup(java.time.Instant now) throws Exception {
		var m = cleanupJob.getClass().getDeclaredMethod("cleanup", java.time.Instant.class);
		m.setAccessible(true);
		m.invoke(cleanupJob, now);
	}

	@Test
	void uploadedImageIsServed() throws Exception {
		// Ảnh mẫu đã được chép vào nơi lưu ảnh khi khởi động
		mvc.perform(get("/uploads/bath.jpg"))
			.andExpect(status().isOk())
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
				.contentType(MediaType.IMAGE_JPEG));
		mvc.perform(get("/uploads/khong-co.jpg")).andExpect(status().isNotFound());
		mvc.perform(get("/uploads/..%2Fapplication.yml")).andExpect(status().is4xxClientError());

		byte[] png = image("png", 40, 30, true);
		var file = new org.springframework.mock.web.MockMultipartFile("file", "a.png", "image/png", png);
		var multipart = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/uploads").file(file);
		mvc.perform(multipart).andExpect(status().isUnauthorized());

		// PNG có nền trong suốt: giữ PNG, kích thước không đổi
		String auth = login("admin", "admin123");
		String body = mvc.perform(multipart.header("Authorization", auth))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		String url = JsonPath.read(body, "$.url");
		assertThat(url).startsWith("/uploads/").endsWith(".png");
		var served = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(
			mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray()));
		assertThat(served.getWidth()).isEqualTo(40);
		assertThat(served.getColorModel().hasAlpha()).isTrue();
	}

	@Test
	void uploadsAreResizedAndValidated() throws Exception {
		String auth = login("admin", "admin123");
		var upload = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/uploads");

		// Ảnh lớn -> thu nhỏ còn cạnh dài 1920px, lưu JPEG
		byte[] big = image("jpg", 4000, 3000, false);
		String body = mvc.perform(upload.file(new org.springframework.mock.web.MockMultipartFile("file", "big.jpg", "image/jpeg", big))
				.header("Authorization", auth))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String url = JsonPath.read(body, "$.url");
		assertThat(url).endsWith(".jpg");
		var res = mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse();
		var img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(res.getContentAsByteArray()));
		assertThat(img.getWidth()).isEqualTo(1920);
		assertThat(img.getHeight()).isEqualTo(1440);

		// PNG không trong suốt -> đổi sang JPEG cho nhẹ
		body = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/uploads")
				.file(new org.springframework.mock.web.MockMultipartFile("file", "a.png", "image/png", image("png", 50, 50, false)))
				.header("Authorization", auth))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		assertThat((String) JsonPath.read(body, "$.url")).endsWith(".jpg");

		// Đuôi .png nhưng nội dung không phải ảnh -> từ chối
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/uploads")
				.file(new org.springframework.mock.web.MockMultipartFile("file", "x.png", "image/png", "<script>".getBytes()))
				.header("Authorization", auth))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("File không phải ảnh hợp lệ"));
	}

	@Test
	void adminCanManageUsers() throws Exception {
		String auth = login("admin", "admin123");

		mvc.perform(get("/api/auth/me").header("Authorization", auth))
			.andExpect(jsonPath("$.fullName").value("Quản trị viên"));
		mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());

		// Thiếu họ tên và mật khẩu -> 400 theo từng trường
		mvc.perform(post("/api/users").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"letan\",\"fullName\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.fullName").exists());
		mvc.perform(post("/api/users").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"letan\",\"fullName\":\"Lễ Tân\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.password").exists());

		String created = mvc.perform(post("/api/users").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"letan\",\"fullName\":\"Nguyễn Lễ Tân\",\"password\":\"matkhau1\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.fullName").value("Nguyễn Lễ Tân"))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(created, "$.id");

		// Trùng tên đăng nhập
		mvc.perform(post("/api/users").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"letan\",\"fullName\":\"X\",\"password\":\"matkhau1\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.username").exists());

		// Đổi họ tên + mật khẩu, rồi đăng nhập bằng mật khẩu mới: token có họ tên mới
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/users/" + id)
				.header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"fullName\":\"Trần Lễ Tân\",\"password\":\"matkhau2\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("letan"))
			.andExpect(jsonPath("$.fullName").value("Trần Lễ Tân"));
		var letanLogin = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"letan\",\"password\":\"matkhau2\"}"))
			.andReturn().getResponse();
		String other = "Bearer " + JsonPath.read(letanLogin.getContentAsString(), "$.token");
		String letanRefresh = letanLogin.getCookie(REFRESH_COOKIE).getValue();
		refresh(letanRefresh).andExpect(status().isOk());

		// Không tự xóa tài khoản đang đăng nhập
		mvc.perform(delete("/api/users/" + id).header("Authorization", other))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Không thể tự xóa tài khoản đang đăng nhập"));

		// Xóa tài khoản kéo theo xóa mọi phiên đăng nhập của tài khoản đó
		mvc.perform(delete("/api/users/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
		mvc.perform(get("/api/users/" + id).header("Authorization", auth)).andExpect(status().isNotFound());
		refresh(letanRefresh).andExpect(status().isUnauthorized());
	}
}
