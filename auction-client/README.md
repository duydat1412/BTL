# Module `auction-client`

Đây là phần mềm đại diện cho **Người Chơi (Client)** để giao tiếp với hệ thống Đấu giá.

## Vai Trò
- Quản lý giao diện người dùng **(GUI)** bằng **JavaFX**.
- Tạo một TCP Client socket để bắn các `Request/Message Object` tới Server ở `localhost:8080` sử dụng cổng giao tiếp `ObjectOutputStream` và `ObjectInputStream` thuần túy.
- Xử lý Thread riêng biệt để làm nhiệm vụ **Lắng nghe thụ động (Observer)** từ Server. Ví dụ khi có một người khác vượt giá bạn, luồng lắng nghe này sẽ nhận lệnh báo động và cập nhật lên màn hình FXML theo thời gian thực (Real-time).

## Cấu trúc các thành phần (Dự kiến do Người C thưc hiện)
```text
src/main/java/com/auction/client/
├── App.java                   ← Nơi chứa phương thức main() khởi Động app JavaFX
├── controllers/               ← Gắn liền với các file giao diện .fxml (Xử lý ấn nút, gõ phím)
├── network/                   ← Nơi thiết lập Socket kết nối tới Server
└── utils/                     ← Tiện ích format thông báo, popup
```
