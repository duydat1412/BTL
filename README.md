# Auction System

## 1. Mô tả bài toán và phạm vi hệ thống

Đây là hệ thống đấu giá trực tuyến xây dựng theo mô hình client-server bằng Java. Hệ thống cho phép nhiều người dùng kết nối đồng thời, đăng nhập theo vai trò, tạo phiên đấu giá, tham gia trả giá, theo dõi cập nhật real-time và quản trị dữ liệu người dùng/phiên đấu giá.

Phạm vi hiện tại của hệ thống:

- Server socket xử lý request/response giữa client và server
- Client JavaFX cho người dùng cuối
- Quản lý người dùng theo vai trò `ADMIN`, `SELLER`, `BIDDER`
- Quản lý item và phiên đấu giá
- Trả giá thủ công, tự động và cập nhật real-time
- Một số chức năng quản trị như ban/unban user, hủy phiên, nạp tiền

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

### Công nghệ sử dụng

- Java 17
- Maven Wrapper
- JavaFX 17
- Java Socket
- Gson
- JUnit 5

### Môi trường chạy

- Windows
- Linux
- macOS

### Yêu cầu cài đặt

- Cài JDK 17 và cấu hình `JAVA_HOME`
- Không bắt buộc cài Maven riêng vì project đã có `mvnw` và `mvnw.cmd`
- Máy cần cho phép mở nhiều terminal để chạy Server và Client riêng

## 3. Cấu trúc thư mục / module chính

```text
BTL/
├── auction-common/   # Class dùng chung: entity, enum, message, request/response
├── auction-server/   # Server socket, handler, service, repository, scheduler
├── auction-client/   # JavaFX UI, controller, network client
├── data/             # Dữ liệu runtime được server sử dụng/lưu trữ
├── docs/             # Tài liệu dự án
├── pom.xml           # Parent POM cho multi-module Maven project
├── mvnw              # Maven wrapper cho Linux/macOS
└── mvnw.cmd          # Maven wrapper cho Windows
```

## 4. Câu lệnh dòng lệnh để chạy chương trình

Các lệnh dưới đây phải chạy tại thư mục gốc của project, nơi chứa file `pom.xml`.

### Build toàn bộ project

Windows:

```powershell
.\mvnw.cmd clean install
```

Linux / macOS:

```bash
./mvnw clean install
```

### Chạy Server

Windows:

```powershell
.\mvnw.cmd -pl auction-server exec:java "-Dexec.mainClass=com.auction.server.AuctionServerApp"
```

Linux / macOS:

```bash
./mvnw -pl auction-server exec:java -Dexec.mainClass=com.auction.server.AuctionServerApp
```

### Chạy Client

Windows:

```powershell
.\mvnw.cmd -pl auction-client javafx:run
```

Linux / macOS:

```bash
./mvnw -pl auction-client javafx:run
```

## 5. Hướng dẫn chạy Server/Client theo thứ tự

1. Mở terminal tại thư mục gốc project.
2. Build toàn bộ project bằng `clean install`.
3. Mở terminal thứ nhất và chạy Server.
4. Chờ Server khởi động xong trên cổng mặc định.
5. Mở terminal thứ hai và chạy Client.
6. Nếu muốn mô phỏng nhiều người dùng, mở thêm terminal và chạy thêm Client.

Khuyến nghị:

- Luôn chạy Server trước, rồi mới chạy Client
- Nếu clone mới project, nên build lại từ đầu trước khi chạy

## 6. Danh sách chức năng đã hoàn thành

- Đăng ký tài khoản
- Đăng nhập hệ thống
- Phân quyền theo vai trò người dùng
- Tạo item
- Tạo phiên đấu giá
- Xem danh sách phiên đấu giá
- Xem chi tiết phiên đấu giá
- Đặt giá thủ công
- Đặt giá tự động
- Cập nhật giá và trạng thái phiên theo thời gian thực
- Dashboard cho seller
- Dashboard cho admin
- Ban user
- Unban user
- Hủy phiên đấu giá
- Nạp tiền cho người dùng
- Theo dõi số dư
- Lưu trữ và đọc dữ liệu hệ thống

## 7. Link báo cáo PDF và video demo

- Báo cáo PDF: 
- Video demo: 
