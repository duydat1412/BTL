# CONTEXT FOR AI ASSISTANT — NGƯỜI A (ĐẠT): TEAM LEAD & SERVER CORE

## THÔNG TIN CHO TRỢ LÝ AI
Bạn đang hỗ trợ **Người A** — Team Lead trong nhóm 4 sinh viên năm nhất thực hiện bài tập lớn môn **Lập trình Nâng cao (Java OOP)**. Dự án: **Hệ thống đấu giá trực tuyến**, kiến trúc Client-Server.

## QUY TẮC
1. Chỉ viết code trong phạm vi nhiệm vụ của Người A (xem bên dưới).
2. KHÔNG viết Entity classes, Design Patterns, Concurrency lock — đó là phần Người B.
3. KHÔNG viết JavaFX UI, FXML, FX Controllers — đó là phần Người C.
4. KHÔNG viết User auth logic, Scheduler, Auto-bid, Anti-sniping — đó là phần Người D.
5. Code dùng **Java 17+**, build bằng **Maven**, convention theo **Google Java Style Guide**.
6. Commit theo **Conventional Commits**: `feat(server): ...`, `fix(repository): ...`
7. Giải thích ngắn gọn, đi thẳng vào code. Sinh viên năm nhất mới học OOP ~5 tuần.

---

## NHIỆM VỤ CỦA NGƯỜI A

### 1. Kiến trúc hệ thống
```
Client (JavaFX) ←── Socket (ObjectStream) ──→ Server ──→ DataFile (.dat)
```
- Kiến trúc Client-Server, giao tiếp qua **Java Socket** (`java.net.Socket` / `java.net.ServerSocket`)
- Server-side: Handler → Service → Repository → DataStore (Serialization)
- Chỉ Server quản lý dữ liệu (qua file .dat)

### 2. Server Infrastructure
- Dùng **Java Socket thuần** (`java.net.ServerSocket` + `java.net.Socket`)
- `ServerSocket` lắng nghe nhiều client, mỗi client xử lý bằng **Thread riêng**
- Giao tiếp giữa client-server bằng `ObjectOutputStream` / `ObjectInputStream` để gửi nhận object
- Chạy trên `localhost:8080`

### 3. Socket Message Handlers
Server nhận object request từ client → xử lý → trả object response:

| Message Type | Chức năng | Request Object | Response Object |
|-------------|-----------|---------------|----------------|
| `REGISTER` | Đăng ký | `{username, password, email, role}` | `{success, message}` |
| `LOGIN` | Đăng nhập | `{username, password}` | `{success, user}` |
| `GET_AUCTIONS` | Danh sách phiên đấu giá | — | `{List<Auction>}` |
| `GET_AUCTION` | Chi tiết phiên | `{auctionId}` | `{Auction}` |
| `CREATE_AUCTION` | Tạo phiên (Seller) | `{itemId, startTime, endTime, startingPrice}` | `{success, auction}` |
| `PLACE_BID` | Đặt giá | `{auctionId, bidderId, amount}` | `{success, message}` |
| `GET_BID_HISTORY` | Lịch sử bid | `{auctionId}` | `{List<BidTransaction>}` |
| `GET_ITEMS` | Danh sách sản phẩm | — | `{List<Item>}` |
| `CREATE_ITEM` | Tạo sản phẩm (Seller) | `{name, description, type, ...}` | `{success, item}` |
| `UPDATE_ITEM` | Sửa sản phẩm | `{itemId, ...}` | `{success}` |
| `DELETE_ITEM` | Xóa sản phẩm | `{itemId}` | `{success}` |

### 4. Realtime Updates (qua Socket + Observer Pattern)
- Server duy trì danh sách connected clients (Socket connections)
- Khi có event (bid mới, phiên kết thúc...) → push tới tất cả client đang watch auction đó
- Events gửi cho client:
  - `NEW_BID`: có bid mới → gửi `{bidderId, amount, timestamp}`
  - `PRICE_UPDATE`: giá hiện tại thay đổi
  - `AUCTION_END`: phiên kết thúc → gửi `{winnerId, finalPrice}`
  - `TIME_EXTENDED`: anti-sniping kéo dài thời gian

### 5. DataStore + Repository Layer (Serialization)
- DataStore: **Singleton pattern** (phối hợp Người B)
- Lưu trữ dữ liệu bằng **Serialization** (`ObjectOutputStream` / `ObjectInputStream`) vào file `.dat`
- Repository interface pattern:
```java
public interface UserRepository {
    void save(User user);
    User findById(String id);
    User findByUsername(String username);
    List<User> findAll();
    void update(User user);
    void delete(String id);
}
```
- Implement `SerializableUserRepository`, `SerializableItemRepository`, v.v.

### 6. Handler Layer
- Nhận object request từ Socket → parse → gọi Service → trả object response
- Service layer do Người B (Concurrency), Người D (Auth, Scheduler) viết logic
- Người A tích hợp Service vào Handler

### 7. Checkstyle + Maven (Tuần 9)
- Tích hợp **Checkstyle plugin** vào `pom.xml`
- Enforce **Google Java Style** tự động khi build (`mvn checkstyle:check`)

### 8. CI/CD (GitHub Actions) — Tuần 9
```yaml
# .github/workflows/build.yml
name: Build & Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn clean test
      - run: mvn checkstyle:check
```

### 9. README.md (Tuần 13-14)
- Viết README.md đầy đủ:
  - Hướng dẫn cài đặt (prerequisites, clone, build)
  - Cách chạy Server
  - Cách chạy Client
  - Cấu trúc project
  - Screenshots (nếu có)

### 10. Team Lead Duties
- Setup GitHub repo + branch protection (`main` cần PR + 1 approval)
- Review Pull Requests từ B, C, D
- Merge nhánh, resolve conflicts giữa các module
- Đảm bảo Google Java Style Guide + Conventional Commits

---

## TỰ HỌC (theo lịch trình)
- **Tuần 9**: Tự học Java Socket server-side (`ServerSocket`, `Socket`, `Thread`), Serialization (`ObjectOutputStream`, `ObjectInputStream`)

---

## CẤU TRÚC THƯ MỤC NGƯỜI A QUẢN LÝ
```
pom.xml                                 ← Parent POM (multi-module) + Checkstyle plugin
.gitignore
README.md                               ← Hướng dẫn đầy đủ (Tuần 13-14)
.github/workflows/build.yml             ← CI/CD

auction-server/
├── pom.xml
└── src/main/java/com/auction/server/
    ├── AuctionServerApp.java            ← Main entry point (ServerSocket)
    ├── datastore/
    │   └── DataStore.java               ← Singleton, Serialization (.dat files)
    ├── repository/
    │   ├── UserRepository.java
    │   ├── ItemRepository.java
    │   ├── AuctionRepository.java
    │   └── BidRepository.java
    └── handler/
        ├── UserHandler.java
        ├── ItemHandler.java
        ├── AuctionHandler.java
        └── BidHandler.java
```

## GIT WORKFLOW (Team Lead)
- Nhánh mình: `feature/A-<mô-tả>` (ví dụ: `feature/A-setup-server`)
- Review PR: kiểm tra code compile, không conflict, đúng scope
- Merge `dev → main` khi đạt milestone (cuối tuần 8, 10, 13)
- Tag version: `git tag v1.0`, `v2.0`, ...
