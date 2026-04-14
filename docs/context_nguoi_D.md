# CONTEXT FOR AI ASSISTANT — NGƯỜI D: FEATURES & QA

## THÔNG TIN CHO TRỢ LÝ AI
Bạn đang hỗ trợ **Người D** trong nhóm 4 sinh viên năm nhất thực hiện bài tập lớn môn **Lập trình Nâng cao (Java OOP)**. Dự án: **Hệ thống đấu giá trực tuyến**, kiến trúc Client-Server.

## QUY TẮC
1. Chỉ viết code trong phạm vi nhiệm vụ của Người D (xem bên dưới).
2. KHÔNG viết code Server infrastructure, Socket, Handler, Repository — đó là phần Người A.
3. KHÔNG viết Entity classes, Design Patterns, Concurrency lock — đó là phần Người B.
4. KHÔNG viết JavaFX UI, FXML, Controllers — đó là phần Người C.
5. Code dùng **Java 17+**, build bằng **Maven**, convention theo **Google Java Style Guide**.
6. Giải thích ngắn gọn, đi thẳng vào code. Sinh viên năm nhất mới học OOP ~5 tuần.

---

## NHIỆM VỤ CỦA NGƯỜI D

### 1. User Management (`server/service/UserService.java`) — Tuần 7-8
- Đăng ký tài khoản: validate username unique, email format, password strength
- Đăng nhập: so sánh password hash
- **Password hashing**: dùng BCrypt
```java
// Đăng ký
String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
// Đăng nhập
boolean match = BCrypt.checkpw(rawPassword, hashedPassword);
```

### 2. Product Management (`server/service/ItemService.java`) — Tuần 7-8
- CRUD validation cho sản phẩm đấu giá
- Validate: tên không rỗng, giá khởi điểm > 0, thời gian hợp lệ
- Chỉ Seller mới được tạo/sửa/xóa sản phẩm của mình

### 3. Custom Exceptions (`server/exception/`) — Tuần 8
- `InvalidBidException` — khi bid < giá hiện tại hoặc dữ liệu bid không hợp lệ
- `AuctionClosedException` — khi bid vào phiên đã FINISHED hoặc CANCELED
- `AuthenticationException` — khi đăng nhập sai hoặc không có quyền

### 4. Session End Logic (`server/service/AuctionScheduler.java`) — Tuần 8-9
- Dùng `ScheduledExecutorService` để tự động đóng phiên khi hết thời gian
- Xác định người thắng cuộc (bidder có giá cao nhất)
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
// Khi tạo phiên mới:
long delay = auction.getEndTime().getTime() - System.currentTimeMillis();
scheduler.schedule(() -> endAuction(auctionId), delay, TimeUnit.MILLISECONDS);
```

### 5. Status Transitions — Tuần 8-9
```
OPEN → RUNNING → FINISHED → PAID / CANCELED
```
- `OPEN`: phiên được tạo, chưa đến giờ bắt đầu
- `RUNNING`: đang diễn ra, nhận bid
- `FINISHED`: hết giờ, xác định người thắng
- `PAID`: người thắng đã thanh toán
- `CANCELED`: phiên bị hủy (không có ai bid, hoặc seller hủy)

### 6. Exception Handling & Edge Cases — Tuần 9-11
| Edge Case | Xử lý |
|-----------|--------|
| Bid < giá hiện tại | Throw `InvalidBidException` |
| Bid khi phiên đã FINISHED | Throw `AuctionClosedException` |
| Seller tự bid sản phẩm mình | Từ chối |
| Dữ liệu rỗng / null | Validate + throw `IllegalArgumentException` |
| Mất kết nối giữa chừng | Catch `IOException`, thông báo client |
| 2 bid cùng lúc, cùng giá | Ưu tiên bid đến trước (timestamp) |
| Đăng nhập sai | Throw `AuthenticationException` |

### 7. Unit Test Coverage ≥ 60% (Tuần 10)
- Phối hợp Người B để đạt coverage ≥ 60%
- Dùng **JaCoCo plugin** để đo coverage
- Viết test cho các edge case ở mục 6

### 8. Auto-Bidding (Nâng cao — 0.5đ bonus) — Tuần 13-14
- Người dùng đặt: `maxBid` (giá tối đa), `increment` (bước giá)
- Khi có bid mới từ đối thủ → hệ thống tự đặt giá = currentPrice + increment
- Dùng `PriorityQueue` sắp xếp theo `registeredAt` (ai đăng ký trước, ưu tiên)
- Không vượt quá `maxBid`
```java
PriorityQueue<AutoBid> queue = new PriorityQueue<>(
    Comparator.comparing(AutoBid::getRegisteredAt)
);
```

### 9. Anti-Sniping (Nâng cao — 0.5đ bonus) — Tuần 13-14
- Nếu có bid trong **X giây cuối** (ví dụ: 30s) → gia hạn thêm **Y giây** (ví dụ: 60s)
```java
long remaining = auction.getEndTime().getTime() - System.currentTimeMillis();
if (remaining < SNIPE_THRESHOLD_MS) {  // 30 * 1000
    auction.setEndTime(new Date(auction.getEndTime().getTime() + EXTENSION_MS));  // 60 * 1000
    // Reschedule trong AuctionScheduler
}
```

### 10. Bid History Chart (Nâng cao — 0.5đ bonus, phối hợp Người C) — Tuần 13-14
- Cung cấp data cho LineChart: List<BidTransaction> sorted by timestamp
- Người C sẽ render chart, Người D cung cấp logic lấy data

---

## CẤU TRÚC THƯ MỤC NGƯỜI D QUẢN LÝ
```
auction-server/src/main/java/com/auction/server/
├── service/
│   ├── UserService.java            ← Đăng ký, đăng nhập, BCrypt
│   ├── ItemService.java            ← CRUD validation sản phẩm
│   ├── AuctionScheduler.java       ← ScheduledExecutorService, anti-sniping
│   └── AutoBidService.java         ← Auto-bid logic, PriorityQueue
└── exception/                      ← Custom exceptions
    ├── InvalidBidException.java
    ├── AuctionClosedException.java
    └── AuthenticationException.java

auction-server/src/test/java/       ← Edge case tests (coverage ≥ 60%, JaCoCo)
```

## ENTITY CLASSES (do Người B tạo, Người D chỉ DÙNG)
```java
import com.auction.common.entity.*;
import com.auction.common.enums.*;
// User, Bidder, Seller, Admin, Item, Auction, BidTransaction, AutoBid
// AuctionStatus: OPEN, RUNNING, FINISHED, PAID, CANCELED
// ⚠️ Tất cả entity implements Serializable
```

## GIT WORKFLOW
- Nhánh: `feature/D-<mô-tả>` (ví dụ: `feature/D-user-auth`)
- Commit: `feat(auth): thêm đăng ký với BCrypt hash`
- Merge vào `dev` qua Pull Request → Người A review.
