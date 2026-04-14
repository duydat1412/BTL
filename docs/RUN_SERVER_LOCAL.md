# Hướng dẫn chạy Socket Server ở localhost

> **QUAN TRỌNG:** Cấu trúc dự án hiện đã chuyển hoàn toàn sang mô hình **Java Socket thuần + Serialization** (để đúng yêu cầu thầy giáo). Server sẽ **KHÔNG CÒN LÀ WEB API** nữa. Bạn **KHÔNG THỂ** vào bằng link `http://localhost:8080/` trên trình duyệt và cũng không gọi bằng Postman được!

Tài liệu này hướng dẫn cách chạy server cũng như cách giao tiếp với server.

---

## 1) Yêu cầu môi trường

- Cài **JDK 17+**
- Có Internet lần đầu để tải các thư viện cơ bản qua Maven

Kiểm tra Java (Mở PowerShell hoặc CMD):
```powershell
java -version
```

---

## 2) Cách chạy Server

Bạn có thể chạy Server bằng một trong hai cách dưới đây:

### Cách 1: Chạy trực tiếp trên IDE (Khuyên dùng)
1. Mở dự án trong IntelliJ IDEA / Eclipse / VS Code.
2. Mở file: `auction-server/src/main/java/com/auction/server/AuctionServerApp.java`.
3. Nhấn nút Run (Biểu tượng tam giác Play) đính kèm cạnh dòng `public static void main`.
4. Console hiện `=== Khởi động Auction Server ===` và `Server đang lắng nghe trên cổng 8080` tức là thành công.

### Cách 2: Chạy qua Terminal (Môi trường Console)
Luôn luôn mở Terminal ở **thư mục gốc** (`D:\BTL>`, không phải `auction-server>`). Đừng đứng ở `auction-server` nếu không nó sẽ bị lỗi không tìm thấy `com.auction.common`.

**Windows (PowerShell/CMD):**
```powershell
# Chạy câu lệnh này ĐÚNG ở thư mục gốc BTL
.\mvnw.cmd clean compile exec:java -pl auction-server -am
```

**macOS / Linux:**
```bash
./mvnw clean compile exec:java -pl auction-server -am
```

---

## 3) Cách giao tiếp (Truy cập) vào Server

Như đã nhấn mạnh ở trên, đây là Raw TCP Socket Server sử dụng `ObjectInputStream/ObjectOutputStream`. 
Do đó chỉ có các chương trình Java gửi tin dạng Object Serialization mới truy cập được.

**Làm sao để test?**
Theo task được phân công ở Tuần 6-7, **Người C** cần được yêu cầu tạo code cho `NetworkClient.java` ở module `auction-client`, mở kết nối tới Server thông qua:
```java
Socket socket = new Socket("localhost", 8080);
ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//... send objects
```
Sau đó anh/chị mới có thể chạy ứng dụng Client để nói chuyện và truyền đi các lệnh (đăng ký, đấu giá) cho hệ thống Server xử lý.

---

## 4) Cách dừng server

- **Trên IDE:** Nhấn nút Stop (ô vuông màu đỏ) ở cửa sổ Console/Run.
- **Trên Terminal:** Nhấn tổ hợp `Ctrl + C`, nếu nó hỏi `Terminate batch job (Y/N)?`, hãy gõ `Y` rồi ấn Enter.

> ⚠️ Khi Server tắt đúng cách, nó sẽ lưu bộ nhớ RAM xuống file Database `data/auction_data.dat`. 

---

## 5) Lỗi thường gặp: `Address already in use`
Mặc định Java Socket Server sẽ xin hệ thống mạng **cổng 8080**. Nếu lỡ chạy đúp 2 lần hoặc server cũ chưa bị tắt hẳn, máy sẽ báo bận.

**Xử lý:** (Chạy cửa sổ PowerShell dưới quyền Admin)
```powershell
$pids = Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($processIdToStop in $pids) { Stop-Process -Id $processIdToStop -Force }
```
Sau đó bật lại Server như Bước 2.
