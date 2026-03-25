package com.auction.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Cấu hình WebSocket — kênh giao tiếp realtime giữa server và client.
 *
 * <p>REST API = client hỏi → server trả lời (một chiều, phải hỏi mới có)
 * <p>WebSocket = server có thể CHỦ ĐỘNG gửi dữ liệu cho client (hai chiều)
 *
 * <p>Ví dụ: Khi ai đó đặt giá mới, server lập tức gửi thông báo
 * cho TẤT CẢ client đang xem phiên đấu giá đó — không cần client hỏi.
 *
 * <p>Tuần 1 chưa dùng WebSocket, nhưng cấu hình sẵn khung để tuần 4-5 chỉ cần
 * thêm handler vào là xong.
 *
 * <p>@EnableWebSocket = bật tính năng WebSocket trong Spring Boot
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // TODO: Tuần 4-5 sẽ đăng ký WebSocket handler ở đây
        // Ví dụ: registry.addHandler(auctionHandler, "/ws/auction/{id}")
        //                 .setAllowedOrigins("*");
    }
}
