# Báo cáo Tiến độ Dự án — Cập nhật lần cuối: 26/05/2026

> **Build Status:** ✅ BUILD SUCCESS (4/4 modules)

---

## Kiến trúc tổng quan

```
Client (JavaFX) ←── Socket (ObjectStream) ──→ Server ──→ DataFile (.dat)
```

3 module Maven: `auction-common` | `auction-server` | `auction-client`

---

## Server (Backend) — ✅ 100%

| File | Chức năng | Trạng thái |
|------|----------|-----------|
| `AuctionServerApp.java` | ServerSocket port 8080, multi-thread | ✅ |
| `ClientHandler.java` | 17 actions dispatch (REGISTER, LOGIN, GET_USERS, BAN_USER, UNBAN_USER, CANCEL_AUCTION, GET_AUCTIONS, GET_AUCTION, CREATE_AUCTION, PLACE_BID, GET_BID_HISTORY, GET_ITEMS, CREATE_ITEM, UPDATE_ITEM, DELETE_ITEM, REGISTER_AUTO_BID, REMOVE_AUTO_BID) | ✅ |
| `ClientRegistry.java` | Singleton quản lý client connections + broadcast push | ✅ |
| `DataStore.java` | Thread-safe Singleton, Serialization ra file .dat | ✅ |
| `UserRepository` / `ItemRepository` / `AuctionRepository` / `BidRepository` | CRUD qua Serializable implementations | ✅ |
| `UserService.java` | BCrypt login/register, validation | ✅ |
| `ItemService.java` | Full CRUD + auto tạo Auction khi tạo Item | ✅ |
| `AuctionService.java` | createAuction, getAuctions, getAuction, cancelAuction | ✅ |
| `BidService.java` | ReentrantLock per auction, Strategy pattern | ✅ |
| `AuctionScheduler.java` | ScheduledExecutorService, OPEN→RUNNING→FINISHED, anti-sniping (30s/30s) | ✅ |
| `AutoBidService.java` | Observer pattern, PriorityQueue | ✅ |
| `AuctionEventManager.java` + `BroadcastObserver.java` | Observer pattern, push realtime | ✅ |
| Custom Exceptions | `AuthenticationException`, `InvalidBidException`, `AuctionClosedException` | ✅ |

---

## Client (Frontend) — ✅ ~90%

| Màn hình | FXML | Controller | Kết nối server? | Ghi chú |
|---------|------|-----------|----------------|---------|
| Login | `login.fxml` | `LoginController.java` | ✅ | Navigate theo role (BIDDER→list, SELLER→dashboard, ADMIN→admin) |
| Register | `register.fxml` | `RegisterController.java` | ✅ | Validate + gửi server, combo box BIDDER/SELLER |
| Auction List | `auction_list.fxml` | `AuctionListController.java` | ✅ | Load từ server, push listener, click→detail |
| Auction Detail | `auction_detail.fxml` | `AuctionDetailController.java` | ✅ | PlaceBid gọi server, push listener cập nhật giá/end realtime |
| Seller Dashboard | `seller_dashboard.fxml` | `SellerDashboardController.java` | ✅ | Load items, create item + auto auction |
| Create Item | `create_item.fxml` | `CreateItemController.java` | ✅ | Form + image picker + duration |
| Admin Panel | `admin.fxml` | `AdminController.java` | ❌ | Chỉ print console, chưa có logic thực tế |
| `NetworkClient.java` | — | — | ✅ | Singleton, async, push listener, ReentrantLock serialization |
| `style.css` | — | — | ✅ | CSS Glassmorphism |

---

## Common (Shared) — ✅ ~95%

| Package | Nội dung | Trạng thái |
|---------|---------|-----------|
| `entity/` | User→Bidder/Seller/Admin, Item→Electronics/Art/Vehicle, Auction, BidTransaction, AutoBid | ✅ |
| `enums/` | AuctionStatus, UserRole, ItemType | ✅ |
| `message/` | ClientRequest, ClientResponse, ServerPushMessage + 10+ Request classes | ✅ |
| `factory/` | ItemFactory | ✅ |
| `observer/` | AuctionObserver interface | ✅ |
| `strategy/` | BidStrategy, ManualBidStrategy, AutoBidStrategy | ✅ |

---

## Còn thiếu sót

1. **AdminController.java** — cần implement: thống kê (user count, auction count), quản lý user (ban/unban), quản lý auction (cancel)
2. **Auto-bid UI** — backend đã có, frontend chưa có nút bật/tắt auto-bid trên màn hình detail
3. **Bid History chart** — bonus feature (0.5đ)
4. **CSS polish** — animation, hover effects
5. **README.md** — cần viết hướng dẫn đầy đủ
