/**
 * Package chứa Service layer — xử lý logic nghiệp vụ.
 *
 * <p>Service nằm giữa Controller và DAO:
 * Controller → Service → DAO → Database
 *
 * <p>Ví dụ: Khi người dùng đặt giá:
 * 1. Controller nhận request "đặt giá 100k"
 * 2. Service kiểm tra: giá có hợp lệ? phiên đấu giá còn mở?
 * 3. Nếu OK → DAO lưu vào database
 *
 * <p>Sẽ được triển khai ở Tuần 4-5 (phối hợp Người B, D).
 */
package com.auction.server.service;
