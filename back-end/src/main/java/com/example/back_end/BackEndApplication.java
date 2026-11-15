package com.example.back_end;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class BackEndApplication {

	public static void main(String[] args) {
		// Windows báo múi giờ "Asia/Saigon" (tên cũ) mà PostgreSQL không nhận -> dùng tên chuẩn
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(BackEndApplication.class, args);
	}

}
