# Tổng kết công việc Tuần 1 — Người A (Team Lead)

Chúc mừng! Mình đã hoàn thành toàn bộ khung xương (scaffold) cho dự án. Dưới đây là giải thích chi tiết từng phần dành cho bạn (mới học Java/OOP 5 tuần).

## 1. Cấu trúc Maven Multi-module (Đa mô-đun)
Thay vì làm 1 project khổng lồ, mình chia thành 3 phần nhỏ:
- **`auction-common`**: Chứa code dùng chung (entities, enums). Cả server và client đều dùng phần này.
- **`auction-server`**: Bộ não của hệ thống (xử lý logic, lưu database).
- **`auction-client`**: Giao diện người dùng (JavaFX) — phần của Người C.

**Lợi ích**: Giúp nhóm 4 người làm việc song song mà không bị chồng chéo code.

---

## 2. File [pom.xml](file:///d:/BTL/pom.xml) là gì?
Hãy coi [pom.xml](file:///d:/BTL/pom.xml) như một **"thực đơn"** hoặc **"danh sách đi chợ"**.
- Nó báo cho Maven biết dự án cần những "nguyên liệu" (thư viện) nào.
- Ví dụ: Trong server, mình khai báo cần `spring-boot-starter-web`. Maven sẽ tự động lên mạng tải thư viện đó về cho mình. Bạn không cần tự copy file `.jar` thủ công nữa.

---

## 3. Spring Boot — "Động cơ" của Server
Bạn mới học Java cơ bản, thông thường tạo server rất khó. Nhưng **Spring Boot** giúp mình:
- Tự chuẩn bị sẵn một Web Server (Tomcat) bên trong.
- Bạn chỉ cần viết code xử lý, Spring Boot lo việc lắng nghe kết nối từ internet.
- [AuctionServerApp.java](file:///d:/BTL/auction-server/src/main/java/com/auction/server/AuctionServerApp.java): Đây là nút "ON" của toàn bộ hệ thống. Chạy file này là server bắt đầu hoạt động.

---

## 4. Database (Cơ sở dữ liệu) H2
- **H2** là một database "nhúng". Nó không cần cài đặt phần mềm nặng nề. Dữ liệu sẽ được lưu thẳng vào một file trong thư mục dự án.
- [application.properties](file:///d:/BTL/auction-server/src/main/resources/application.properties): Nơi cấu hình "địa chỉ" lưu file và mật khẩu.
- [schema.sql](file:///d:/BTL/auction-server/src/main/resources/schema.sql): Các câu lệnh SQL để tự động tạo bảng (users, items, auctions...) mỗi khi server bật lên.

---

## 5. REST API & Health Check
- Client muốn "nói chuyện" với Server thì dùng URL (giống như vào website).
- Mình đã tạo một [HealthController.java](file:///d:/BTL/auction-server/src/main/java/com/auction/server/controller/HealthController.java). Khi bạn chạy server và vào link `http://localhost:8080/api/health`, bạn sẽ thấy server phản hồi lại.
- **Kết quả chạy thử**: Server đã chạy thành công trên máy bạn!
  - **Địa chỉ**: `http://localhost:8080/api/health`
  - **Phản hồi**: `{"message":"Auction Server đang chạy!","status":"UP",...}`

---

## 6. GitHub Actions (CI/CD)
- File [.github/workflows/build.yml](file:///d:/BTL/.github/workflows/build.yml) là một **"người gác cổng"**.
- Mỗi khi bạn push code lên GitHub, GitHub sẽ tự động chạy thử `mvn clean compile` để kiểm tra xem code của bạn có bị lỗi không. Nếu lỗi, nó sẽ báo đỏ để nhóm biết và sửa ngay.

---

## Kết quả kiểm tra (Verification)
Mình đã chạy thử lệnh build và kết quả như sau:
```text
[INFO] Reactor Summary for auction-system 1.0-SNAPSHOT:
[INFO] auction-system ..................................... SUCCESS
[INFO] auction-common ..................................... SUCCESS
[INFO] auction-server ..................................... SUCCESS
[INFO] auction-client ..................................... SUCCESS
[INFO] BUILD SUCCESS
```
**=> Toàn bộ khung dự án đã sẵn sàng cho tuần 2!**
