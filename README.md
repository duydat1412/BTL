#  Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)
**Bài Tập Lớn - Môn Lập Trình Nâng Cao (Java OOP)**

Một hệ thống đấu giá thời gian thực (real-time) được xây dựng hoàn toàn bằng Java, áp dụng kiến trúc mô hình Client-Server, đa luồng (Multithreading) và giao diện JavaFX.

##  Danh sách thành viên nhóm
* **Người A - Team Lead & Server:** Cấu trúc hệ thống, Socket/REST API, Xử lý giao dịch cốt lõi.
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

## Hướng dẫn cài đặt và chạy thử nghiệm
### Yêu cầu môi trường
* Đã cài đặt JDK (phiên bản 17 trở lên).
* Có kết nối Internet để Maven tải các thư viện cần thiết.

### Các bước khởi chạy
1. **Clone dự án về máy:**
   ```bash
   git clone https://github.com/duydat1412/BTL.git

## Các IDE hỗ trợ việc vibe code nên dùng : 

### AntiGravity model Claude Opus 4.6 
**Nhược điểm:**
1. Free quotas ít, hết thì phải đợi cooldown có khi lên tới 7 ngày - 30 ngày.
2. Hết quotas cho ClaudeOpus4.6 thì phải dùng Gemini 3 Flash.

**Ưu điểm:**
1. Claude Opus 4.6 cho hiệu suất cao, xử lí được các tác vụ logic phức tạp.
2. Lên plan rõ ràng trước khi code.
3. Chỉ cần đọc context là hiểu project đang làm gì.

### VSCode Copilot Pro 
**Nhược điểm:**
1. Chỉ thực sự mạnh nếu như có Pro ( có thể có được free nếu có github student pack, chưa có github student pack thì lên mạng học cách đăng kí free )
2. Limit 300 Premium Requests / tháng. Dùng hết sẽ không thể tiếp tục dùng Chat với các model cao cấp.
3. Nếu dùng model Clade Opus 4.6 thì chỉ tương đương 100 Requests.

**Ưu điểm:**
1. Nếu hết Premium Requests thì có thể dùng model Gemini 3.1 Pro hoặc Claude Sonnet 4.6, hiệu suất cũng khá cao.
2. Có hỗ trợ autocompletion unlimited ( gợi ý code khi gõ trong VSCode ).
