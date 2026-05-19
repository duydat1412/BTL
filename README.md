# 🏛️ Hệ thống Đấu giá Trực tuyến (Auction System)

Ứng dụng đấu giá trực tuyến xây dựng bằng Java, sử dụng kiến trúc Client-Server với Java Socket và JavaFX.

## 📋 Yêu cầu hệ thống

- **Java**: JDK 17 trở lên
- **Maven**: Đã tích hợp Maven Wrapper (`mvnw`) — không cần cài riêng

## 🚀 Hướng dẫn chạy

### Bước 1: Build toàn bộ dự án
```bash
.\mvnw clean compile
```

### Bước 2: Chạy Server (Terminal 1)
```bash
.\mvnw exec:java -pl auction-server "-Dexec.mainClass=com.auction.server.AuctionServerApp"
```
Server sẽ lắng nghe trên cổng **8080**.

### Bước 3: Chạy Client (Terminal 2)
```bash
.\mvnw javafx:run -pl auction-client
```
Mở thêm terminal khác và chạy lại lệnh trên để mô phỏng nhiều người dùng cùng lúc.

> **Lưu ý**: Tất cả lệnh phải chạy từ **thư mục gốc** của project (nơi chứa file `pom.xml` chính).

## 📁 Cấu trúc dự án

```
BTL/
├── auction-common/     # Shared: Entity, Message, Enum, Design Patterns
├── auction-server/     # Server: Socket, Handler, Service, Repository, DataStore
├── auction-client/     # Client: JavaFX UI, Controller, NetworkClient
├── docs/               # Tài liệu, sơ đồ, báo cáo tiến độ
└── pom.xml             # Maven parent POM (multi-module)
```

## 🏗️ Kiến trúc

| Tầng | Mô tả |
|------|-------|
| **Client (JavaFX)** | Giao diện người dùng, gửi `ClientRequest` qua Socket |
| **ClientHandler** | Dispatcher nhận request, phân loại theo `Action`, chuyển cho Service |
| **Service** | Xử lý nghiệp vụ (validation, business logic) |
| **Repository** | Thao tác CRUD trên DataStore (RAM) |
| **DataStore** | Singleton lưu trữ dữ liệu, Serialization xuống file `.dat` |

## 🎨 Design Patterns đã áp dụng

- **Factory Method** — `ItemFactory` tạo Electronics/Art/Vehicle
- **Observer** — `AuctionEventManager` thông báo khi có bid mới
- **Strategy** — `ManualBidStrategy` / `AutoBidStrategy`
- **Singleton** — `DataStore`, `NetworkClient`

## 🧪 Chạy Unit Test

```bash
.\mvnw test -pl auction-server
```

## 👥 Phân công

| Thành viên | Vai trò |
|-----------|---------|
| **A** | Team Lead, Server Core, ClientHandler, CI/CD |
| **B** | OOP Entities, Design Patterns, Unit Tests |
| **C** | Client UI/UX (JavaFX, FXML, CSS) |
| **D** | Business Logic (Services), Scheduler, QA |
