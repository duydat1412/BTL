# CONTEXT FOR AI ASSISTANT — NGƯỜI B: OOP & DATA MODEL

## THÔNG TIN CHO TRỢ LÝ AI
Bạn đang hỗ trợ **Người B** trong nhóm 4 sinh viên năm nhất thực hiện bài tập lớn môn **Lập trình Nâng cao (Java OOP)**. Dự án: **Hệ thống đấu giá trực tuyến** (Online Auction System), kiến trúc Client-Server.

## QUY TẮC
1. Chỉ viết code trong phạm vi nhiệm vụ của Người B (xem bên dưới).
2. KHÔNG viết code phần Server (Socket, Handler, Repository) — đó là phần của Người A.
3. KHÔNG viết code JavaFX/FXML/UI — đó là phần của Người C.
4. KHÔNG viết code User auth, Scheduler, Auto-bid — đó là phần của Người D.
5. Code dùng **Java 17+**, build bằng **Maven**, convention theo **Google Java Style Guide**.
6. Giải thích ngắn gọn, đi thẳng vào code. Đây là sinh viên năm nhất mới học OOP ~5 tuần.

---

## NHIỆM VỤ CỦA NGƯỜI B

### 1. Entity Hierarchy (package `com.auction.common.entity`) — Tuần 6-7
```
Entity (abstract, implements Serializable)
├── User (abstract) → Bidder, Seller, Admin
└── Item (abstract) → Electronics, Art, Vehicle
Auction
BidTransaction
AutoBid
```
- **Encapsulation**: Tất cả fields `private`, có getter/setter.
- **Inheritance**: Phân cấp rõ ràng như trên.
- **Polymorphism**: Override `getInfo()` hoặc `toString()` ở mỗi subclass.
- **Abstraction**: Dùng abstract class/interface cho hành vi chung.
- ⚠️ Tất cả entity phải `implements Serializable` để hỗ trợ truyền qua Socket (ObjectStream).

### 2. Enums (package `com.auction.common.enums`) — Tuần 6
- `AuctionStatus`: `OPEN`, `RUNNING`, `FINISHED`, `PAID`, `CANCELED`
- `UserRole`: `BIDDER`, `SELLER`, `ADMIN`
- `ItemType`: `ELECTRONICS`, `ART`, `VEHICLE`

### 3. Design Patterns
| Pattern | Implementation | Tuần |
|---------|---------------|------|
| **Factory Method** | `ItemFactory` → tạo `Electronics`, `Art`, `Vehicle` theo `ItemType` | 6 |
| ⚠️ **Observer** | `AuctionObserver` interface + `AuctionEventManager` → notify khi có bid mới | **7 (ƯU TIÊN CAO)** |
| **Strategy** | `BidStrategy` interface → `ManualBidStrategy`, `AutoBidStrategy` | 8 |
| **Singleton** | Phối hợp Người A cho `DataStore` | 7 |

> ⚠️ **ƯU TIÊN CAO: Phải hoàn thành Observer Pattern trong Tuần 7 theo yêu cầu thầy.**
> Observer Pattern là nền tảng cho realtime update qua Socket. Không hoàn thành đúng hạn sẽ block các task của Người A và Người C.

### 4. Concurrency (trong `server/service/BidService.java`) — Tuần 7-8
- Dùng `ReentrantLock` per Auction để tránh race condition khi nhiều bidder đặt giá cùng lúc.
- Tránh: lost update, giá bị rollback, 2 người cùng thắng.

### 5. Unit Tests (JUnit 5) — Tuần 8, 10
- Test: `UserServiceTest`, `BidServiceTest`, `AuctionServiceTest`, `ItemFactoryTest`
- Test concurrent bidding bằng `ExecutorService` + `CountDownLatch`
- **Unit test coverage ≥ 60%** — Dùng **JaCoCo plugin** để đo coverage (Tuần 10)

---

## CẤU TRÚC THƯ MỤC NGƯỜI B QUẢN LÝ
```
auction-common/src/main/java/com/auction/common/
├── entity/          ← Entity, User, Bidder, Seller, Admin, Item, Electronics, Art, Vehicle, Auction, BidTransaction, AutoBid
├── enums/           ← AuctionStatus, UserRole, ItemType
├── factory/         ← ItemFactory
├── observer/        ← AuctionObserver (interface)
└── strategy/        ← BidStrategy, ManualBidStrategy, AutoBidStrategy

auction-server/src/main/java/com/auction/server/
└── observer/        ← AuctionEventManager (implements Subject)

auction-server/src/test/java/  ← JUnit tests (coverage ≥ 60%)
```

## GIT WORKFLOW
- Nhánh: `feature/B-<mô-tả>` (ví dụ: `feature/B-entity-classes`)
- Commit: `feat(entity): thêm class User hierarchy`
- Merge vào `dev` qua Pull Request → Người A review.
