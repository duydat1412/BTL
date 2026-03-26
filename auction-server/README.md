# Module: `auction-server`

## Vai trò (Role)
Đây là **Máy Chủ (Backend)** – Trái tim và Bộ não của hệ thống. 
Chịu trách nhiệm vận hành mọi logic nghiệp vụ, quản trị dữ liệu tập trung và đảm bảo mọi luật lệ của sàn đấu giá được thực thi nghiêm ngặt. Module này chạy hoàn toàn độc lập với Client.

## Kiến trúc & Công nghệ
Được xây dựng bằng **Spring Boot** với mô hình phân tầng chức năng rõ ràng:
- **`Controller` (Tầng giao tiếp)**: Cung cấp REST API làm điểm tiếp nhận các yêu cầu HTTP (Login, Đặt giá, Lấy lịch sử) gửi từ Máy khách.
- **`Service` (Tầng nghiệp vụ)**: Logic kiểm soát trò chơi. Kiểm định xem bước giá nạp lên có hợp lệ không? Phiên đã hết giờ chưa? Tài khoản này có bị cấm không?
- **`DAO & Database` (Tầng lưu trữ)**: Sử dụng các câu lệnh truy vấn SQL (qua Spring Boot JDBC) để thay đổi/lấy dữ liệu. Dữ liệu này được lưu xuống một cơ sở dữ liệu nhúng **H2 Database** tích hợp sẵn.
- **`WebSocket` (Tầng realtime)**: Ngược lại với API tĩnh, công nghệ này cho phép Máy chủ tự động **Bơm (Push)** dữ liệu biến động (như thông báo có người vừa mua thành công một món đồ) tới tất cả các ứng dụng Máy khách đang xem ngay trong thời gian thực.

## Liên kết hệ thống
- Phụ thuộc (Depend trên) `auction-common`: Kéo bộ khung `Entity` từ common sang để làm khuôn đúc và lưu trữ chúng trực tiếp vào cơ sở dữ liệu.
