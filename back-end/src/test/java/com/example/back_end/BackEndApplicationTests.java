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

	@Test
	void uploadedImageIsServed() throws Exception {
		// Ảnh mẫu đã được chép vào nơi lưu ảnh khi khởi động
		mvc.perform(get("/uploads/bath.jpg"))
			.andExpect(status().isOk())
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
				.contentType(MediaType.IMAGE_JPEG));
		mvc.perform(get("/uploads/khong-co.jpg")).andExpect(status().isNotFound());
		mvc.perform(get("/uploads/..%2Fapplication.yml")).andExpect(status().is4xxClientError());

		byte[] png = { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3 };
		var file = new org.springframework.mock.web.MockMultipartFile("file", "a.png", "image/png", png);
		var multipart = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/uploads").file(file);
		mvc.perform(multipart).andExpect(status().isUnauthorized());

		String body = mvc.perform(multipart.header("Authorization", login("admin", "admin123")))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		String url = JsonPath.read(body, "$.url");
		assertThat(url).startsWith("/uploads/").endsWith(".png");
		assertThat(mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray())
			.isEqualTo(png);
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
		String other = login("letan", "matkhau2");

		// Không tự xóa tài khoản đang đăng nhập
		mvc.perform(delete("/api/users/" + id).header("Authorization", other))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Không thể tự xóa tài khoản đang đăng nhập"));

		mvc.perform(delete("/api/users/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
		mvc.perform(get("/api/users/" + id).header("Authorization", auth)).andExpect(status().isNotFound());
	}
}
