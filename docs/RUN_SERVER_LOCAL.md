# Hướng dẫn chạy Server ở localhost (sau khi clone repo)

Tài liệu này dành cho các thành viên B, C, D sau khi clone dự án về máy.

## 1) Yêu cầu môi trường

- Cài **JDK 17+**
- Có Internet lần đầu để Maven Wrapper tải dependency

Kiểm tra Java:

```powershell
java -version
```

## 2) Clone và vào dự án

```bash
git clone https://github.com/duydat1412/BTL.git
cd BTL
```

## 3) Chạy server (cách khuyến nghị)

### Windows (PowerShell)

```powershell
Set-Location .\auction-server
..\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
cd auction-server
../mvnw spring-boot:run
```

> Server mặc định chạy tại: `http://localhost:8080`

## 4) Kiểm tra server đã chạy chưa

Mở trình duyệt hoặc gọi API health check:

- `http://localhost:8080/api/health`

PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Nếu server chạy, sẽ thấy JSON có `status: "UP"`.

## 5) Cách dừng server

- Ngay tại terminal đang chạy server: nhấn `Ctrl + C`

Nếu bị kẹt tiến trình (không dừng được):

```powershell
$pids = Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($processIdToStop in $pids) { Stop-Process -Id $processIdToStop -Force }
```

## 6) Lỗi thường gặp và cách xử lý

### Lỗi 1: `Database may be already in use` (H2 lock file)

Nguyên nhân: đang có 1 instance server khác chạy và giữ file DB.

Cách xử lý nhanh:

1. Dừng server cũ theo mục 5.
2. Chạy lại server.

### Lỗi 2: `Address already in use: bind` (port 8080 bận)

Cách 1 (khuyến nghị): dừng process đang dùng port 8080 theo mục 5.

Cách 2: đổi port trong file:

- `auction-server/src/main/resources/application.properties`

Ví dụ:

```properties
server.port=8081
```

rồi chạy lại và test ở `http://localhost:8081/api/health`.

## 7) Ghi chú cho team

- Không cần cài Maven thủ công, vì đã dùng Maven Wrapper (`mvnw`/`mvnw.cmd`).
- Luôn chạy server từ module `auction-server` để tránh lỗi plugin khi chạy từ root.
