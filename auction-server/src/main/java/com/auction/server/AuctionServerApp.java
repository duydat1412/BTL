package com.auction.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm bắt đầu (entry point) của server.
 *
 * <p>Annotation @SpringBootApplication tương đương nói với Java:
 * "Đây là ứng dụng Spring Boot — hãy tự động cấu hình mọi thứ cho tôi."
 *
 * <p>Khi chạy hàm main(), Spring Boot sẽ:
 * 1. Khởi tạo một web server (Tomcat) nhúng bên trong
 * 2. Lắng nghe HTTP request trên port 8080
 * 3. Tự tìm tất cả Controller, Service, DAO trong package này
 */
@SpringBootApplication
public class AuctionServerApp {

    public static void main(String[] args) {
        // SpringApplication.run() = "bật server lên"
        SpringApplication.run(AuctionServerApp.class, args);
        System.out.println("=== Auction Server đã khởi động tại http://localhost:8080 ===");
    }
}
