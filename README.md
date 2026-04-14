# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)
**Bài Tập Lớn - Môn Lập Trình Nâng Cao (Java OOP)**

Một hệ thống đấu giá thời gian thực (real-time) được xây dựng hoàn toàn bằng Java, áp dụng kiến trúc mô hình Java TCP Socket, đa luồng (Multithreading), cơ sở dữ liệu qua Java Serialization (.dat) và giao diện JavaFX.

## 👥 Danh sách thành viên nhóm
* **Người A - Team Lead & Server:** Cấu trúc hệ thống, Java Socket đa luồng (ClientHandler), Thread pool, Thiết lập Singleton DataStore.
* **Người B - OOP & Model:** Xây dựng cấu trúc Object/Entity (Serializable), Factory, Observer Pattern (Cập nhật giá Real-time).
* **Người C - Client & GUI:** Thiết kế giao diện JavaFX, kết nối NetworkClient (ObjectStream) để nói chuyện với Server.
* **Người D - Features & QA:** Quản lý logic nghiệp vụ các Handler, thuật toán Auto-bidding, Anti-sniping, Custom Exceptions và Unit Test.

## 🛠 Công nghệ sử dụng
* **Ngôn ngữ:** Java (JDK 17+)
* **Giao diện (GUI):** JavaFX & FXML (Scene Builder)
* **Quản lý dự án:** Maven multi-module
* **Kiến trúc mạng:** Raw TCP Socket (ServerSocket & Socket) giao tiếp bằng ObjectOutputStream / ObjectInputStream.
* **Lưu trữ dữ liệu:** Java Serialization (Ghi xuất Object vào file tĩnh).
* **Kiến trúc:** Client-Server, MVC Pattern.

## 🚀 Hướng dẫn cài đặt và chạy thử nghiệm
### Yêu cầu môi trường
* Đã cài đặt JDK (phiên bản 17 trở lên).
* Cửa sổ dòng lệnh CMD/PowerShell hoặc Terminal có hỗ trợ chạy script.

### Cấu trúc thư mục (Multi-module)
- `auction-common`: Chứa Entity, Enum và Interfaces dùng chung.
- `auction-server`: Chứa Main App mở Socket Server lắng nghe port 8080 và quản lý file data.
- `auction-client`: Chứa giao diện hiển thị cho người chơi (JavaFX) và trình gửi/nhận Request.
- `docs/`: Tài liệu đồ án chi tiết.

### Cách chạy Server cục bộ (Chỉ dành cho Test hoặc cho Backend/Server Auth)
1. **Clone dự án về máy:**
   ```bash
   git clone https://github.com/duydat1412/BTL.git
   cd BTL
   ```
2. **Khởi chạy Socket Server:**
   Đứng tại thư mục **BTL**, chạy lệnh sau để build và boot server (chỉ lắng nghe, đợi Client nối vào):
   ```powershell
   .\mvnw.cmd clean compile exec:java -pl auction-server -am
   ```
   Nếu console in ra `Server đang lắng nghe trên cổng 8080` tức là thành công!

> **Chi tiết các lệnh và báo lỗi**: Xem thêm ở [docs/RUN_SERVER_LOCAL.md](docs/RUN_SERVER_LOCAL.md)