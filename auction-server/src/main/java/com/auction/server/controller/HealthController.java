package com.auction.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller kiểm tra server có hoạt động hay không.
 *
 * <p>@RestController = nói Spring: "Class này xử lý HTTP request và trả về JSON"
 * <p>@GetMapping("/api/health") = "Khi ai đó gọi GET http://localhost:8080/api/health,
 *    hãy chạy hàm bên dưới và trả kết quả về cho họ"
 *
 * <p>Dùng để test nhanh: mở trình duyệt → gõ http://localhost:8080/api/health
 * → nếu thấy JSON → server đang chạy OK.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
            "status", "UP",
            "message", "Auction Server đang chạy!",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
