# Hướng dẫn chạy Socket Server ở localhost

Cấu trúc dự án hiện đã chuyển sang mô hình **Java Socket thuần + Serialization**, không còn dùng Spring Boot.
Tài liệu này hướng dẫn cách chạy server cũng như cách khắc phục lỗi cơ bản.

## 1) Yêu cầu môi trường

- Cài **JDK 17+**
- Có Internet lần đầu để tải các thư viện cơ bản qua Maven

Kiểm tra Java:

```powershell
java -version
```

## 2) Cách chạy Server

Bạn có thể chạy Server bằng một trong hai cách dưới đây:

### Cách 1: Chạy trực tiếp trên IDE (Khuyến nghị)
1. Mở dự án trong IntelliJ IDEA / Eclipse / VS Code.
2. Tìm tới class `AuctionServerApp` trong file: `auction-server/src/main/java/com/auction/server/AuctionServerApp.java`
3. Nhấn nút Run (Tam giác màu xanh) hoặc chuột phải chọn `Run 'AuctionServerApp.main()'`.
4. Nếu Console in ra `=== Khởi động Auction Server ===` và `Server đang lắng nghe trên cổng 8080`, tức là server đã chạy thành công.

### Cách 2: Chạy qua Terminal (Môi trường Console)
Chạy trực tiếp project thông qua công cụ Maven Exec Plugin mà hệ thống đã cài sẵn.

**Windows (PowerShell/CMD):**
```powershell
# Di chuyển vào module server
cd auction-server

# Build và chạy class main của server (Sử dụng script ./mvnw.cmd có ở root)
..\mvnw.cmd clean compile exec:java
```

**macOS / Linux:**
```bash
cd auction-server
../mvnw clean compile exec:java
```

## 3) Cách dừng server

- **Trên IDE:** Nhấn nút Stop (ô vuông màu đỏ) ở cửa sổ Console/Run.
- **Trên Terminal:** Nhấn tổ hợp phím `Ctrl + C`, nếu terminal hỏi xác nhận `Terminate batch job (Y/N)?`, hãy gõ `Y` rồi Enter.

> ⚠️ Khi Server tắt đúng cách, nó sẽ thực thi lệnh lưu bộ nhớ tạm RAM xuống CSDL file `data/auction_data.dat`. Đừng tắt ngang bằng Task Manager trừ phi bị đơ hoàn toàn.

## 4) Một số lỗi có thể gặp

### Lỗi 1: `Address already in use: JVM_Bind` (Cổng 8080 đang bận)
Mặc định Java Socket Server sẽ xin hệ điều hành cổng số mạng là **8080**. Nếu bạn lỡ chạy 2 lần hoặc bị kẹt tiến trình cũ chưa tắt hẳn thì không thể mở lại.

**Xử lý nhanh:** (trên Windows PowerShell Run as Admin)
```powershell
# Tìm ID tiến trình đang dùng port 8080 và ép tắt
$pids = Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($processIdToStop in $pids) { Stop-Process -Id $processIdToStop -Force }
```
Sau đó chạy lại server.

### Lỗi 2: Không lưu được Data, báo lỗi `FileNotFoundException`
Hãy kiểm tra xem folder `auction-server/data/` đã được cấp quyền đọc ghi dữ liệu chưa. Hệ thống tự tạo folder và file `auction_data.dat` nếu không có. 
