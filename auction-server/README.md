# Module `auction-server`

Đây là module **Backend cốt lõi** phục vụ hệ thống đấu giá, được viết hoàn toàn bằng Java thuần (Core Java).

## Vai Trò
- **Không sử dụng Framework Web/API REST**: Thay vì giao tiếp bằng JSON qua HTTP, dự án sử dụng **Raw TCP Sockets** (`ServerSocket`) mở ở port `8080` lắng nghe các Object Java được gửi trực tiếp.
- Thay vì sử dụng cơ sở dữ liệu dạng bảng như MySQL/H2, server sử dụng **Java Serialization** để thao tác ghi toàn bộ đối tượng Singleton `DataStore` trực tiếp xuống file đuôi `.dat`.
- Áp dụng Threading: Mỗi một Client kết nối tới sẽ được uỷ quyền cho một Thread (luồng) thông qua `ThreadPool` nằm ở trong `AuctionServerApp`. Điều này giúp nhiều khách hàng có thể đấu giá cùng lúc.

## Cấu trúc các thành phần
```text
src/main/java/com/auction/server/
├── AuctionServerApp.java       ← Nơi chứa phương thức main() mở cổng 8080
├── datastore/                  ← DataStore.java (Quản lý việc loadData()/saveData() ra ổ cứng)
├── handler/                    ← ClientHandler.java (Luồng giao tiếp với 1 client chuyên biệt)
└── repository/                 ← Nơi truy xuất danh sách và thao tác CRUD vào System RAM
```

## Cách biên dịch và khởi chạy
Hãy di chuyển ra thư mục gốc (`D:\BTL`) và gọi lệnh Maven sau:
```powershell
.\mvnw.cmd clean compile exec:java -pl auction-server -am
```
> Server sẽ đợi lệnh `Object` từ Client chứ không phản hồi nếu gõ trên trình duyệt web trình bày UI HTTP.
