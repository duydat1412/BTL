# CONTEXT FOR AI ASSISTANT — NGƯỜI A (ĐẠT): TEAM LEAD & SERVER CORE

## THÔNG TIN CHO TRỢ LÝ AI
Bạn đang hỗ trợ **Người A** — Team Lead trong nhóm 4 sinh viên năm nhất thực hiện bài tập lớn môn **Lập trình Nâng cao (Java OOP)**. Dự án: **Hệ thống đấu giá trực tuyến**, kiến trúc Client-Server.

## QUY TẮC
1. Chỉ viết code trong phạm vi nhiệm vụ của Người A (xem bên dưới).
2. KHÔNG viết Entity classes, Design Patterns, Concurrency lock — đó là phần Người B.
3. KHÔNG viết JavaFX UI, FXML, FX Controllers — đó là phần Người C.
4. KHÔNG viết User auth logic, Scheduler, Auto-bid, Anti-sniping — đó là phần Người D.
5. Code dùng **Java 17+**, build bằng **Maven**, convention theo **Google Java Style Guide**.
6. Commit theo **Conventional Commits**: `feat(server): ...`, `fix(dao): ...`
7. Giải thích ngắn gọn, đi thẳng vào code. Sinh viên năm nhất mới học OOP ~5 tuần.

---

## NHIỆM VỤ CỦA NGƯỜI A

### 1. Kiến trúc hệ thống
```
Client (JavaFX) ←── REST + WebSocket ──→ Server ──→ Database
```
- Kiến trúc Client-Server, giao tiếp qua REST API (CRUD) + WebSocket (realtime)
- Server-side MVC: Controller → Service → DAO → Database
- Chỉ Server truy cập database

### 2. Server Infrastructure
- Dùng **Spring Boot** (khuyến nghị) hoặc `com.sun.net.httpserver.HttpServer`
- Nếu Spring Boot: `spring-boot-starter-web` + `spring-boot-starter-websocket`
- Chạy trên `localhost:8080`

### 3. REST API Endpoints
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| `POST` | `/api/auth/register` | Đăng ký | `{username, password, email, role}` |
| `POST` | `/api/auth/login` | Đăng nhập | `{username, password}` |
| `GET` | `/api/auctions` | Danh sách phiên đấu giá | — |
| `GET` | `/api/auctions/{id}` | Chi tiết phiên | — |
| `POST` | `/api/auctions` | Tạo phiên (Seller) | `{itemId, startTime, endTime, startingPrice}` |
| `POST` | `/api/auctions/{id}/bid` | Đặt giá | `{bidderId, amount}` |
| `GET` | `/api/auctions/{id}/history` | Lịch sử bid | — |
| `GET` | `/api/items` | Danh sách sản phẩm | — |
| `POST` | `/api/items` | Tạo sản phẩm (Seller) | `{name, description, type, ...}` |
| `PUT` | `/api/items/{id}` | Sửa sản phẩm | `{...}` |
| `DELETE` | `/api/items/{id}` | Xóa sản phẩm | — |

### 4. WebSocket (Realtime Updates)
- Endpoint: `/ws/auction/{auctionId}`
- Events gửi cho client:
  - `NEW_BID`: có bid mới → gửi `{bidderId, amount, timestamp}`
  - `PRICE_UPDATE`: giá hiện tại thay đổi
  - `AUCTION_END`: phiên kết thúc → gửi `{winnerId, finalPrice}`
  - `TIME_EXTENDED`: anti-sniping kéo dài thời gian

### 5. Database + DAO Layer
- Database: **H2** (embedded, file-based) — không cần cài đặt riêng
- Tables: `users`, `items`, `auctions`, `bid_transactions`, `auto_bids`
```java
// DAO interface pattern:
public interface UserDAO {
    void save(User user);
    User findById(String id);
    User findByUsername(String username);
    List<User> findAll();
    void update(User user);
    void delete(String id);
}
```
- DatabaseManager: **Singleton pattern** (phối hợp Người B)

### 6. Controller Layer
- Nhận HTTP request → parse JSON → gọi Service → trả JSON response
- Service layer do Người B (Concurrency), Người D (Auth, Scheduler) viết logic
- Người A tích hợp Service vào Controller

### 7. CI/CD (GitHub Actions)
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
```

### 8. Team Lead Duties
- Setup GitHub repo + branch protection (`main` cần PR + 1 approval)
- Review Pull Requests từ B, C, D
- Merge nhánh, resolve conflicts giữa các module
- Đảm bảo Google Java Style Guide + Conventional Commits

---

## CẤU TRÚC THƯ MỤC NGƯỜI A QUẢN LÝ
```
pom.xml                                 ← Parent POM (multi-module)
.gitignore
README.md
.github/workflows/build.yml             ← CI/CD

auction-server/
├── pom.xml
└── src/main/java/com/auction/server/
    ├── AuctionServerApp.java            ← Main entry point
    ├── config/                          ← Server config, CORS, WebSocket config
    ├── controller/
    │   ├── UserController.java
    │   ├── ItemController.java
    │   ├── AuctionController.java
    │   └── BidController.java
    ├── db/
    │   └── DatabaseManager.java         ← Singleton, H2 connection
    ├── dao/
    │   ├── UserDAO.java
    │   ├── ItemDAO.java
    │   ├── AuctionDAO.java
    │   └── BidDAO.java
    └── websocket/
        └── AuctionWebSocketHandler.java
```

## GIT WORKFLOW (Team Lead)
- Nhánh mình: `feature/A-<mô-tả>` (ví dụ: `feature/A-setup-server`)
- Review PR: kiểm tra code compile, không conflict, đúng scope
- Merge `dev → main` khi đạt milestone (cuối tuần 3, 5, 7)
- Tag version: `git tag v1.0`, `v2.0`, ...
