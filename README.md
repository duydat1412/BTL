#  Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)
**Bài Tập Lớn - Môn Lập Trình Nâng Cao (Java OOP)**

Một hệ thống đấu giá thời gian thực (real-time) được xây dựng hoàn toàn bằng Java, áp dụng kiến trúc mô hình Client-Server, đa luồng (Multithreading) và giao diện JavaFX.

##  Danh sách thành viên nhóm
* **Người A (Đạt) - Team Lead & Server:** Cấu trúc hệ thống, Socket/REST API, Xử lý giao dịch cốt lõi.
* **Người B - OOP & Model:** Xây dựng Class Hierarchy, Design Patterns, Xử lý Concurrency (Đa luồng).
* **Người C - Client & GUI:** Thiết kế giao diện JavaFX, kết nối Client-Server, cập nhật UI Real-time.
* **Người D - Features & QA:** Quản lý User/Item, thuật toán Auto-bidding, Anti-sniping, Unit Test.

## Công nghệ sử dụng
* **Ngôn ngữ:** Java (JDK 17+)
* **Giao diện (GUI):** JavaFX & FXML (Scene Builder)
* **Quản lý dự án:** Maven
* **Kiến trúc:** Client-Server, MVC Pattern
* **Kỹ thuật nâng cao:** Multithreading (Xử lý đa luồng an toàn bằng `synchronized`/`ReentrantLock`), Socket/Observer Pattern (Cập nhật giá Real-time).

## Tính năng nổi bật
1. **Đấu giá thời gian thực:** Nhiều người dùng có thể cùng lúc đặt giá, màn hình tự động nhảy số không cần tải lại trang.
2. **Xử lý đồng thời an toàn:** Đảm bảo không bị sai lệch dữ liệu khi hàng trăm request trả giá gửi lên cùng một phần nghìn giây.
3. **Đồng hồ đếm ngược:** Mỗi phiên đấu giá có thời gian chạy lùi chính xác. Tự động chốt phiên khi về 0.
4. **Auto-bidding (Tự động trả giá):** Ủy quyền cho hệ thống tự động nâng giá theo bước giá thiết lập sẵn.
5. **Anti-sniping (Chống nẫng tay trên):** Tự động gia hạn thêm 30 giây nếu có lượt trả giá ở những giây cuối cùng.

## ⚙Hướng dẫn cài đặt và chạy thử nghiệm
### Yêu cầu môi trường
* Đã cài đặt JDK (phiên bản 17 trở lên).
* Có kết nối Internet để Maven tải các thư viện cần thiết.

### Các bước khởi chạy
1. **Clone dự án về máy:**
   ```bash
   git clone [https://github.com/duydat1412/BTL.git](https://github.com/duydat1412/BTL.git)