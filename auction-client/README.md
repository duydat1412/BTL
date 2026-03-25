# 💻 Module: `auction-client`

## 📖 Vai trò (Role)
Đây là **Ứng dụng Máy Khách (Frontend)** – Giao diện (Mặt tiền) tiếp xúc trực tiếp với người chơi hoặc quản trị viên (Admin).

## 🏗️ Chức năng chính
Nếu như Server là những đoạn mã chạy câm lặng bên dưới, thì Client làm nhiệm vụ tương tác qua hình ảnh và thao tác phím/chuột. Chức năng chính bao gồm:
- **Tiếp nhận thao tác:** Lắng nghe người dùng nhập liệu mật khẩu, bấm nút bấm (Button), nhập số tiền mong muốn đặt...
- **Giao diện hiển thị (UI):** Vẽ ra một sàn đấu giá giả lập (có thể bằng Java Swing, JavaFX, hoặc giao diện Console) để người dùng xem Danh sách Vật phẩm, Bảng xếp hạng giá và đồng hồ đếm ngược.

## 🔗 Luồng hoạt động & Giao tiếp
Module này **KHÔNG** chứa bất kỳ Database hay kiểm tra logic phức tạp nào, mọi thứ đều dựa vào Server:
1. Client cần hiển thị bảng xếp hạng => Gửi API yêu cầu Server cung cấp.
2. Server gom một cục JSON trả về.
3. Client bắt lấy file JSON đó, dùng khuôn có sẵn từ `auction-common` cùng sự trợ giúp của thư viện `Gson` để "thông dịch" và vẽ ra các chỉ số tương ứng lên màn hình.
4. Client đăng ký một đường dây nóng `WebSocket` ngay khi mở ứng dụng. Bất cứ khi nào đối thủ trả giá hớ, Server sẽ réo qua đường dây nóng này để màn hình tự động nhảy số mới mà không cần F5 (tải lại trang).
