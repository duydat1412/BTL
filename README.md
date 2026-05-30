# Auction System

## 1. Mo ta bai toan va pham vi he thong

Day la he thong dau gia truc tuyen xay dung theo mo hinh client-server bang Java. He thong cho phep nhieu nguoi dung ket noi dong thoi, dang nhap theo vai tro, tao san pham, tao phien dau gia, tham gia tra gia va theo doi cap nhat theo thoi gian thuc.

Pham vi hien tai cua he thong:

- Server socket xu ly request/response giua client va server
- Client JavaFX cho nguoi dung cuoi
- Quan ly nguoi dung theo vai tro `ADMIN`, `SELLER`, `BIDDER`
- Quan ly item va phien dau gia
- Tra gia thu cong, auto-bid va cap nhat real-time
- Chuc nang quan tri nhu ban/unban user, huy phien, nap tien

## 2. Cong nghe su dung, moi truong chay va yeu cau cai dat

### Cong nghe su dung

- Java 17
- Maven Wrapper
- JavaFX 17
- Java Socket
- Gson
- JUnit 5

### Moi truong chay

- Windows
- Linux
- macOS

### Yeu cau cai dat

- Cai JDK 17 va cau hinh `JAVA_HOME`
- Khong bat buoc cai Maven rieng vi project da co `mvnw` va `mvnw.cmd`

## 3. Cau truc thu muc / module chinh

```text
BTL/
├── auction-common/   # Class dung chung: entity, enum, message, request/response
├── auction-server/   # Server socket, handler, service, repository, scheduler
├── auction-client/   # JavaFX UI, controller, network client
├── pom.xml           # Parent POM cho multi-module Maven project
├── mvnw              # Maven wrapper cho Linux/macOS
└── mvnw.cmd          # Maven wrapper cho Windows
```

Luu y:

- Thu muc `data/` se duoc tao tu dong khi server chay lan dau
- File du lieu `auction_data.dat` cung duoc tao tu dong trong qua trinh chay

## 4. Cau lenh dong lenh de chay chuong trinh

Tat ca cac lenh duoi day phai chay tai thu muc goc cua project, noi chua file `pom.xml`.

### Build toan bo project

Windows:

```powershell
.\mvnw.cmd clean install
```

Linux / macOS:

```bash
./mvnw clean install
```

### Chay Server

Windows:

```powershell
.\mvnw.cmd -pl auction-server exec:java "-Dexec.mainClass=com.auction.server.AuctionServerApp"
```

Linux / macOS:

```bash
./mvnw -pl auction-server exec:java -Dexec.mainClass=com.auction.server.AuctionServerApp
```

### Chay Client

Windows:

```powershell
.\mvnw.cmd -pl auction-client javafx:run
```

Linux / macOS:

```bash
./mvnw -pl auction-client javafx:run
```

## 5. Huong dan chay Server/Client theo thu tu

1. Mo terminal tai thu muc goc project.
2. Build toan bo project bang `clean install`.
3. Mo terminal thu nhat va chay Server.
4. Cho Server khoi dong xong tren cong mac dinh `8080`.
5. Mo terminal thu hai va chay Client.
6. Neu muon mo phong nhieu nguoi dung, mo them terminal va chay them Client.

Tai khoan admin mac dinh:

- Username: `admin`
- Password: `Admin@123`

## 6. Danh sach chuc nang da hoan thanh

- Dang ky tai khoan
- Dang nhap he thong
- Phan quyen theo vai tro nguoi dung
- Tao item
- Tao phien dau gia
- Xem danh sach phien dau gia
- Xem chi tiet phien dau gia
- Dat gia thu cong
- Dang ky auto-bid
- Go auto-bid
- Cap nhat gia va trang thai phien theo thoi gian thuc
- Dashboard cho seller
- Dashboard cho admin
- Ban user
- Unban user
- Huy phien dau gia
- Nap tien cho nguoi dung
- Theo doi so du
- Luu tru va doc du lieu he thong
