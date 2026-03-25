package com.auction.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS (Cross-Origin Resource Sharing).
 *
 * <p>Vấn đề: Mặc định, trình duyệt/client KHÔNG cho phép gọi API
 * từ một địa chỉ khác (ví dụ: client ở port 3000 gọi server ở port 8080).
 * Đây là cơ chế bảo mật của trình duyệt.
 *
 * <p>Giải pháp: Cấu hình này nói với server:
 * "Cho phép BẤT KỲ ai gọi API của tôi" — phù hợp khi đang phát triển.
 *
 * <p>@Configuration = nói Spring: "Đây là file cấu hình, hãy đọc nó khi khởi động"
 */
@Configuration
public class CorsConfig {

    /**
     * @Bean = nói Spring: "Tạo object này và quản lý nó cho tôi"
     * WebMvcConfigurer = interface cho phép tuỳ chỉnh cách server xử lý HTTP
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")    // Áp dụng cho mọi URL bắt đầu bằng /api/
                        .allowedOrigins("*")       // Cho phép mọi nguồn gọi tới
                        .allowedMethods("GET", "POST", "PUT", "DELETE")  // Các phương thức HTTP cho phép
                        .allowedHeaders("*");      // Cho phép mọi header
            }
        };
    }
}
