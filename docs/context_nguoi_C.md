# CONTEXT FOR AI ASSISTANT — NGƯỜI C: CLIENT & GUI

## THÔNG TIN CHO TRỢ LÝ AI
Bạn đang hỗ trợ **Người C** trong nhóm 4 sinh viên năm nhất thực hiện bài tập lớn môn **Lập trình Nâng cao (Java OOP)**. Dự án: **Hệ thống đấu giá trực tuyến**, kiến trúc Client-Server.

## QUY TẮC
1. Chỉ viết code trong phạm vi nhiệm vụ của Người C (xem bên dưới).
2. KHÔNG viết code Server, REST API, Database, DAO — đó là phần Người A.
3. KHÔNG viết Entity classes, Design Patterns, Concurrency — đó là phần Người B.
4. KHÔNG viết logic User auth, Auto-bid, Scheduler — đó là phần Người D.
5. Code dùng **Java 17+**, **JavaFX**, **FXML** (Scene Builder), build bằng **Maven**.
6. Giải thích ngắn gọn, đi thẳng vào code. Sinh viên năm nhất mới học OOP ~5 tuần.

---

## NHIỆM VỤ CỦA NGƯỜI C

### 1. JavaFX Screens (FXML + Controller)
| Màn hình | File FXML | Controller | Chức năng |
|----------|-----------|------------|-----------|
| Login/Register | `login.fxml` | `LoginController` | Form đăng nhập, đăng ký tài khoản |
| Danh sách đấu giá | `auction_list.fxml` | `AuctionListController` | Grid/ListView phiên đấu giá đang diễn ra |
| Chi tiết + Bidding | `auction_detail.fxml` | `AuctionDetailController` | Thông tin sản phẩm, đặt giá, countdown timer |
| Quản lý SP (Seller) | `seller_dashboard.fxml` | `SellerDashboardController` | Thêm/sửa/xóa sản phẩm đấu giá |
| Admin Panel | `admin.fxml` | `AdminController` | Quản lý users, auctions |

### 2. Client MVC
```
Controller (JavaFX) → Model (client-side data) → NetworkClient (gọi API server)
```
- Controller xử lý sự kiện UI (click, input)
- Model giữ dữ liệu hiện tại (ObservableList, Property)
- NetworkClient gửi HTTP request + nhận WebSocket events

### 3. NetworkClient (`com.auction.client.network`)
- Gọi REST API server: `GET /auctions`, `POST /bid`, `POST /login`, v.v.
- Lắng nghe WebSocket events: `NEW_BID`, `AUCTION_END`, `PRICE_UPDATE`
- Dùng `HttpClient` (Java 11+) hoặc thư viện đơn giản

### 4. Realtime Update UI
```java
// Khi nhận event từ WebSocket → cập nhật UI trên JavaFX thread:
Platform.runLater(() -> {
    priceLabel.setText("Giá hiện tại: " + newPrice);
    countdownLabel.setText(remainingTime);
    bidHistoryList.add(newBid);
});
```
- Countdown timer: dùng `Timeline` hoặc `AnimationTimer`
- Cập nhật giá tự động khi có bid mới (KHÔNG polling liên tục)

### 5. Error Handling UI
- Hiển thị `Alert` dialog khi có lỗi (bid thất bại, mất kết nối, v.v.)
- Validate input trước khi gửi (giá phải là số, giá > giá hiện tại, v.v.)
- Disable nút bid khi phiên đã kết thúc

### 6. CSS Styling
- Dark theme hoặc modern look
- File: `resources/css/style.css`

---

## CẤU TRÚC THƯ MỤC NGƯỜI C QUẢN LÝ
```
auction-client/src/main/
├── java/com/auction/client/
│   ├── AuctionClientApp.java        ← JavaFX Application (main)
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── AuctionListController.java
│   │   ├── AuctionDetailController.java
│   │   ├── SellerDashboardController.java
│   │   └── AdminController.java
│   ├── model/                       ← Client-side data models
│   └── network/
│       └── NetworkClient.java       ← REST + WebSocket client
└── resources/
    ├── view/                        ← FXML files
    │   ├── login.fxml
    │   ├── auction_list.fxml
    │   ├── auction_detail.fxml
    │   ├── seller_dashboard.fxml
    │   └── admin.fxml
    └── css/
        └── style.css
```

## ENTITY CLASSES (do Người B tạo, Người C chỉ DÙNG)
```java
// Import từ common module:
import com.auction.common.entity.*;
import com.auction.common.enums.*;
// Các class: User, Bidder, Seller, Admin, Item, Auction, BidTransaction
// Enums: AuctionStatus, UserRole, ItemType
```

## GIT WORKFLOW
- Nhánh: `feature/C-<mô-tả>` (ví dụ: `feature/C-login-screen`)
- Commit: `feat(ui): hoàn thành màn hình login`
- Merge vào `dev` qua Pull Request → Người A review.
